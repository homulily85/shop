package com.shop.webserver;

import java.util.Map;

/**
 * Represents an HTTP request received by the web server.
 *
 * @param path    The path of the requested resource (e.g., "/products").
 * @param method  The HTTP method used for the request (e.g., "GET", "POST").
 * @param query   A map of query parameters included in the request URL.
 * @param headers A map of HTTP headers included in the request.
 * @param body    The body of the request, if applicable (e.g., for POST requests).
 */
public record HttpRequest(
        String path,
        String method,
        Map<String, String> query,
        Map<String, String> headers,
        String body
) {
}