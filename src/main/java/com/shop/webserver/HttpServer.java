package com.shop.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private final Map<String, RequestHandler> routes;
    private final ServerSocket serverSocket;

    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.routes = new HashMap<>();
    }

    private static String parseRequestBody(BufferedReader in, int contentLength) throws IOException {
        char[] bodyChars = new char[contentLength];
        in.read(bodyChars, 0, contentLength);
        return new String(bodyChars);
    }

    private static Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx != -1) {
                String headerName = line.substring(0, idx).trim();
                String headerValue = line.substring(idx + 1).trim();
                headers.put(headerName, headerValue);

            }
        }
        return headers;
    }

    private static Map<String, String> parseQueryParams(String rawPath) {
        Map<String, String> queryParams = new HashMap<>();
        if (rawPath.contains("?")) {
            String queryString = rawPath.split("\\?", 2)[1];
            for (String param : queryString.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    queryParams.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }

        return queryParams;
    }

    private static String formatHttpResponse(HttpResponse response) {
        String contentType = "application/json";

        return "HTTP/1.1 " + response.statusCode() + " " + response.statusMessage() + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + response.body().length() + "\r\n" +
                "\r\n" +
                response.body();
    }

    public void addRoute(String path, RequestHandler handler) {
        routes.put(path, handler);
    }

    private HttpResponse dispatch(String path, String method, Map<String, String> queryParams,
                                  Map<String, String> headers, String body) {

        Map<String, String> pathParams = new HashMap<>();
        RequestHandler handler = matchRoute(path, pathParams);

        if (handler != null) {
            return handler.handle(method, queryParams, pathParams, headers, body);
        } else {
            return new HttpResponse(404, "Not Found", """
                {
                    "message": "Route not found."
                }
                """);
        }
    }

    private RequestHandler matchRoute(String requestPath, Map<String, String> pathParams) {
        if (routes.containsKey(requestPath)) {
            return routes.get(requestPath);
        }

        // Dynamic path: Iterate through registered routes
        for (Map.Entry<String, RequestHandler> entry : routes.entrySet()) {
            String routePattern = entry.getKey();
            if (isMatch(routePattern, requestPath, pathParams)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private boolean isMatch(String routePattern, String requestPath,
                            Map<String, String> pathParams) {
        String[] patternSegments = routePattern.split("/");
        String[] requestSegments = requestPath.split("/");

        if (patternSegments.length != requestSegments.length) {
            return false;
        }

        for (int i = 0; i < patternSegments.length; i++) {
            String pattern = patternSegments[i];
            String request = requestSegments[i];

            if (pattern.startsWith(":")) {
                String paramName = pattern.substring(1);
                pathParams.put(paramName, request);
            } else if (!pattern.equals(request)) {
                return false;
            }
        }
        return true;
    }

    public void start() throws IOException {
        System.out.println("HTTP Server started on port " + serverSocket.getLocalPort());

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedReader in =
                     new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out =
                     new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            String requestLine = in.readLine();
            System.out.println("Request: " + requestLine);

            if (requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;

            String method = parts[0]; // Method
            String rawPath = parts[1]; // API path + queryParams
            String pathOnly = rawPath.split("\\?")[0]; // API path

            Map<String, String> queryParams = parseQueryParams(rawPath);
            Map<String, String> headers = parseHeaders(in);
            String requestBody = parseRequestBody(in, headers.get("Content-Length") != null ?
                    Integer.parseInt(headers.get("Content-Length")) : 0);

            HttpResponse responseData = dispatch(pathOnly, method, queryParams, headers,
                    requestBody);

            String httpResponseString = formatHttpResponse(responseData);

            out.write(httpResponseString);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}