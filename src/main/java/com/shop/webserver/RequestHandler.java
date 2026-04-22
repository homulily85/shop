package com.shop.webserver;

import java.util.Map;

@FunctionalInterface
public interface RequestHandler {
    HttpResponse handle(String method,
                        Map<String, String> queryParams,
                        Map<String, String> pathParams,
                        Map<String, String> headers,
                        String body);
}