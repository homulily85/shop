package com.shop.webserver;

import java.util.Map;

public record HttpRequest(
        String path,
        String method,
        Map<String, String> query,
        Map<String, String> headers,
        String body
) {
}