package com.shop.webserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private final Map<String, RequestHandler> routes;
    private final ServerSocket serverSocket;

    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.routes = new HashMap<>();
    }

    public void addRoute(String path, RequestHandler handler) {
        routes.put(path, handler);
    }

    private HttpResponse dispatch(HttpRequest httpRequest) {
        Map<String, String> pathParams = new HashMap<>();
        RequestHandler handler = matchRoute(httpRequest.path(), pathParams);

        if (handler != null) {
            byte[] decodedBody = new byte[0];
            if (httpRequest.body() != null && !httpRequest.body().isBlank()) {
                decodedBody = Base64.getDecoder().decode(httpRequest.body());
            }

            return handler.handle(httpRequest.method(),
                    httpRequest.query(),
                    pathParams,
                    httpRequest.headers(),
                    decodedBody);
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return new HttpResponse(404, "Not Found",
                        mapper.writeValueAsString(Map.of("message", "Route not found.")));
            } catch (JsonProcessingException e) {
                return new HttpResponse(404, "Not Found", "");
            }
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

            ObjectMapper objectMapper = new ObjectMapper();

            HttpRequest httpRequest = objectMapper.readValue(requestLine, HttpRequest.class);

            HttpResponse responseData = dispatch(httpRequest);

            String httpResponseString = objectMapper.writeValueAsString(responseData);

            out.write(httpResponseString);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}