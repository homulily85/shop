<?php

$host = getenv('JAVA_HOST');
$port = (int)getenv('JAVA_PORT');

define('JAVA_HOST', $host);
define('JAVA_PORT', $port);

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