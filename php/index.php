<?php

require_once "config.php";

$method = $_SERVER["REQUEST_METHOD"];
$uri = $_SERVER['REQUEST_URI'];
$path = parse_url($uri, PHP_URL_PATH);

$queryParams = $_GET;
$queryParams = empty($_GET) ? new stdClass() : $_GET;

$headers = function_exists('getallheaders') ? getallheaders() : [];

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

if (preg_match('#^/(cart|checkout)/([^/]+)#', $path, $matches)) {
    $requestedId = $matches[2];

    if ($userId !== $requestedId) {
        http_response_code(403);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(["error" => "Forbidden: You are not authorized to access this resource."]);
        exit;
    }
}

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
    "method"  => $method,
    "path"    => $path,
    "query"   => $queryParams,
    "headers" => $headersForBackend,
    "body"    => base64_encode(file_get_contents('php://input'))
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