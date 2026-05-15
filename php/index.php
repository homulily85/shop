<?php

function rate_limit($redis, $limit = 100, $window = 60): bool
{
    $ip = $_SERVER['REMOTE_ADDR'];
    $key = "rate_limit:" . $ip;

    $current = $redis->incr($key);

    if ($current == 1) {
        $redis->expire($key, $window);
    }

    if ($current > $limit) {
        return false;
    }

    return true;
}


function call_java($data)
{
    $socket = @fsockopen(JAVA_HOST, JAVA_PORT, $errno, $errstr, 5);

    if (!$socket) {
        return json_encode([
            "status" => "ERROR",
            "message" => "Cannot connect to Java shop server at " . JAVA_HOST . ":" . JAVA_PORT,
            "debug_error" => $errstr
        ]);
    }

    fwrite($socket, json_encode($data) . "\n");
    $response = fgets($socket);
    fclose($socket);

    return $response;
}

$host = getenv('JAVA_HOST');
$port = (int)getenv('JAVA_PORT');

define('JAVA_HOST', $host);
define('JAVA_PORT', $port);

$redis = new Redis();
$redis->connect(getenv('REDIS_HOST') ?: '127.0.0.1', (int) getenv('REDIS_PORT') ?: 6379);

$method = $_SERVER["REQUEST_METHOD"];
$uri = $_SERVER['REQUEST_URI'];
$path = parse_url($uri, PHP_URL_PATH);

$queryParams = empty($_GET) ? new stdClass() : $_GET;
$headers = function_exists('getallheaders') ? getallheaders() : [];
$rawBody = file_get_contents('php://input');

$signature = $headers['X-Internal-Signature'] ?? null;
$timestamp = $headers['X-Internal-Timestamp'] ?? null;
$kongApiKey = $headers['X-Kong-Api-Key'] ?? null;
$isInternalCall = ($signature !== null && $timestamp !== null);
$isKongCall = false;

if ($isInternalCall) {
    $secret = getenv('INTERNAL_API_SECRET');
    if (!$secret) {
        http_response_code(500);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(["error" => "Server misconfigured"]);
        exit;
    }

    $now = round(microtime(true) * 1000);
    if (abs($now - (int)$timestamp) > 300000) {
        http_response_code(403);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(["error" => "Request expired"]);
        exit;
    }

    $payload = $timestamp . ":" . $rawBody;
    $expectedSignature = base64_encode(hash_hmac('sha256', $payload, $secret, true));
    if (!hash_equals($expectedSignature, $signature)) {
        http_response_code(403);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(["error" => "Invalid signature"]);
        exit;
    }

    error_log("[HMAC] Validated inter-service request: $method $uri");
}

if (preg_match('#^/bill/([^/]+)/payment-result/?$#', $path)) {
    if (!$isInternalCall) {
        http_response_code(403);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(["error" => "Forbidden: Only internal services can access this endpoint."]);
        exit;
    }
}

if (!$isInternalCall && !rate_limit($redis, 100, 60)) {
    http_response_code(429);
    echo json_encode([
        "status" => "ERROR",
        "message" => "Too many requests"
    ]);
    exit;
}

if (!$isInternalCall) {
    $expectedKongKey = getenv('KONG_API_KEY');
    if ($expectedKongKey && $kongApiKey === $expectedKongKey) {
        $isKongCall = true;
    }
}

if (!$isInternalCall && !$isKongCall) {
    http_response_code(403);
    error_log("[AUTH] Denied unauthenticated request: $method $uri");
    echo json_encode(["status" => "ERROR", "message" => "Forbidden"]);
    exit;
}

$headers['X-Internal-Source'] = $isInternalCall ? 'HMAC' : 'KONG';

$userId = null;
$userRole = null;
foreach ($headers as $key => $value) {
    $lowerKey = strtolower($key);
    if ($lowerKey === 'x-user-id') {
        $userId = $value;
    } elseif ($lowerKey === 'x-user-role') {
        $userRole = strtoupper($value);
    }
}

// Check Cart / Checkout permissions
if (preg_match('#^/(cart|checkout)/([^/]+)#', $path, $matches)) {
    $requestedId = $matches[2];

    if ($userId !== $requestedId) {
        http_response_code(403);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(["error" => "Forbidden: You are not authorized to access this resource."]);
        exit;
    }
}

// Check Product Admin permissions
$isProductRoot = preg_match('#^/products/?$#', $path);
$isProductWithId = preg_match('#^/products/([^/]+)$#', $path);

if (($method === 'POST' && $isProductRoot) ||
    (in_array($method, ['PATCH', 'DELETE']) && $isProductWithId)) {

    if ($userRole !== 'ADMIN') {
        http_response_code(403);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(["error" => "Forbidden: You do not have the required admin privileges."]);
        exit;
    }
}

$headersForBackend = empty($headers) ? new stdClass() : $headers;

$request = [
    "method" => $method,
    "path" => $path,
    "query" => $queryParams,
    "headers" => $headersForBackend,
    "body" => base64_encode($rawBody)
];

error_log($uri);

$rawJavaResponse = call_java($request);

if (!$rawJavaResponse) {
    http_response_code(502);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["error" => "Failed to communicate with Java backend."]);
    exit;
}

$responseData = json_decode($rawJavaResponse, true);

if (is_array($responseData) && isset($responseData['statusCode'])) {
    http_response_code($responseData['statusCode']);
    header('Content-Type: application/json; charset=utf-8');
    echo $responseData['body'];
} else {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["error" => "Invalid response format from backend."]);
}