package com.shop.webserver;

/**
 * Represents an HTTP response that the web server will send back to the client.
 *
 * @param statusCode    The HTTP status code of the response (e.g., 200, 404).
 * @param statusMessage A human-readable message corresponding to the status code (e.g., "OK", "Not Found").
 * @param body          The body of the response, which can contain data such as HTML, JSON, or plain text.
 */
public record HttpTextResponse(int statusCode, String statusMessage, String body) implements HttpResponse {
}