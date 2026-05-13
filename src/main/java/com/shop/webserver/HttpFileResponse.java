package com.shop.webserver;

import java.util.Map;

/**
 * Represents an HTTP response that the web server will send back to the client.
 *
 * @param statusCode    The HTTP status code of the response (e.g., 200, 404).
 * @param headers       A map of HTTP headers to include in the response (e.g., "Content-Type", "Content-Length").
 * @param statusMessage A human-readable message corresponding to the status code (e.g., "OK", "Not Found").
 * @param body          A byte array representing the body of the response.
 */
public record HttpFileResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) implements HttpResponse {
}