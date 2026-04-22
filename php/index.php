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

$request = [
    "method" => $method,
    "path" => $path,
    "query" => $queryParams,
    "body" => file_get_contents('php://input'),
    "headers" => $headers
];

error_log($uri);

$rawJavaResponse = call_java($request);

if (!$rawJavaResponse) {
    http_response_code(502); // Bad Gateway
    echo json_encode(["error" => "Failed to communicate with Java backend."]);
    exit;
}

$responseData = json_decode($rawJavaResponse, true);

if (is_array($responseData) && isset($responseData['statusCode'])) {

    http_response_code($responseData['statusCode']);

    // Set the Content-Type header so the browser/client knows it's receiving JSON data
    header('Content-Type: application/json; charset=utf-8');

    // Output ONLY the body contents, dropping the Java wrapper
    echo $responseData['body'];

} else {
    // Fallback if Java sends back malformed JSON or crashes
    http_response_code(500); // Internal Server Error
    echo json_encode(["error" => "Invalid response format from backend."]);
}