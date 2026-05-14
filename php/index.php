<?php

require_once "config.php";

$method = $_SERVER["REQUEST_METHOD"];
$uri = $_SERVER['REQUEST_URI'];
$path = parse_url($uri, PHP_URL_PATH);

$queryParams = $_GET;
$queryParams = empty($_GET) ? new stdClass() : $_GET;
$headers = function_exists('getallheaders') ? getallheaders() : [];
$headers = empty($headers) ? new stdClass() : $headers;

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
        echo json_encode(["error" => "Server misconfigured"]);
        exit;
    }

    // Validate timestamp (5 minute window)
    $now = round(microtime(true) * 1000);
    if (abs($now - (int)$timestamp) > 300000) {
        http_response_code(403);
        echo json_encode(["error" => "Request expired"]);
        exit;
    }

    // Validate HMAC signature
    $payload = $timestamp . ":" . $rawBody;
    $expectedSignature = base64_encode(hash_hmac('sha256', $payload, $secret, true));
    if (!hash_equals($expectedSignature, $signature)) {
        http_response_code(403);
        echo json_encode(["error" => "Invalid signature"]);
        exit;
    }

    error_log("[HMAC] Validated inter-service request: $method $uri");
}

if (!$isInternalCall) {
    $expectedKongKey = getenv('KONG_API_KEY');
    if ($expectedKongKey && $kongApiKey === $expectedKongKey) {
        $isKongCall = true;
    }
}

// Reject unauthenticated requests
if (!$isInternalCall && !$isKongCall) {
    http_response_code(403);
    error_log("[AUTH] Denied unauthenticated request: $method $uri");
    echo json_encode(["error" => "Forbidden"]);
    exit;
}

$request = [
    "method"  => $method,
    "path"    => $path,
    "query"   => $queryParams,
    "headers" => $headers,
    "body"    => base64_encode($rawBody)
];

error_log($uri);

$rawJavaResponse = call_java($request);

if (!$rawJavaResponse) {
    http_response_code(502);
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
    echo json_encode(["error" => "Invalid response format from backend."]);
}