package com.shop.webserver;

import java.util.Map;

@FunctionalInterface
public interface RequestHandler {
    /**
     * Handles an incoming HTTP request and generates an appropriate HTTP response.
     *
     * @param method      The HTTP method of the request (e.g., "GET", "POST").
     * @param queryParams A map of query parameters included in the request URL.
     * @param pathParams  A map of path parameters extracted from the request URL.
     * @param headers     A map of HTTP headers included in the request.
     * @param body        The body of the request as a byte array, which can be used for processing
     *                    data such as file uploads or JSON payloads.
     * @return An HttpResponse object containing the status code, status message, and response body.
     * @throws Exception If any error occurs during request handling, an exception can be thrown to
     *                   indicate a failure in processing the request.
     */
    HttpResponse handle(String method,
                            Map<String, String> queryParams,
                            Map<String, String> pathParams,
                            Map<String, String> headers,
                            byte[] body) throws Exception;
}