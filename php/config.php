<?php

$host = getenv('JAVA_HOST') ?: '127.0.0.1';
$port = getenv('JAVA_PORT') ? (int)getenv('JAVA_PORT') : 12345;

define('JAVA_HOST', $host);
define('JAVA_PORT', $port);

function call_java($data)
{
    // Supress warnings with @ if you don't want PHP warning logs on connection failure
    $socket = @fsockopen(JAVA_HOST, JAVA_PORT, $errno, $errstr, 5);

    if (!$socket) {
        return json_encode([
            "status" => "ERROR",
            "message" => "Cannot connect to Java shop server at " . JAVA_HOST . ":" . JAVA_PORT,
            "debug_error" => $errstr // Optional: helps with debugging Docker networking issues
        ]);
    }

    fwrite($socket, json_encode($data) . "\n");
    $response = fgets($socket);
    fclose($socket);

    return $response;
}