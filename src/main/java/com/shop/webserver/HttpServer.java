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

    /**
     * Dispatches the incoming HTTP request to the appropriate handler based on the request path.
     *
     * @param httpRequest The incoming HTTP request to be dispatched.
     * @return An HttpResponse object containing the status code, status message, and response body.
     */
    private HttpResponse dispatch(HttpRequest httpRequest) {
        Map<String, String> pathParams = new HashMap<>();
        RequestHandler handler = matchRoute(httpRequest.path(), pathParams);
        ObjectMapper mapper = new ObjectMapper();

        if (handler != null) {
            byte[] decodedBody = new byte[0];
            if (httpRequest.body() != null && !httpRequest.body().isBlank()) {
                decodedBody = Base64.getDecoder().decode(httpRequest.body());
            }

            try {
                return handler.handle(httpRequest.method(),
                        httpRequest.query(),
                        pathParams,
                        httpRequest.headers(),
                        decodedBody);

            } catch (IllegalArgumentException | JsonProcessingException e) {
                try {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Invalid request " +
                                                                                "data";
                    return new HttpResponse(400, "Bad Request",
                            mapper.writeValueAsString(Map.of("error", errorMsg)));
                } catch (Exception ex) {
                    return new HttpResponse(400, "Bad Request", "");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Internal Server " +
                                                                                "Error";
                    return new HttpResponse(500, "Internal Server Error",
                            mapper.writeValueAsString(Map.of("error", errorMsg)));
                } catch (Exception ex) {
                    return new HttpResponse(500, "Internal Server Error", "");
                }
            }
        } else {
            try {
                return new HttpResponse(404, "Not Found",
                        mapper.writeValueAsString(Map.of("message", "Route not found.")));
            } catch (Exception e) {
                return new HttpResponse(404, "Not Found", "");
            }
        }
    }

    /**
     * Matches the incoming request path against registered routes, including support for dynamic
     * path parameters.
     *
     * @param requestPath The path of the incoming HTTP request.
     * @param pathParams  A map to store any extracted path parameters if a dynamic route is
     *                    matched.
     * @return The RequestHandler associated with the matched route, or null if no match is found.
     */
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

    /**
     * Checks if the request path matches the route pattern, which may include dynamic segments
     * (e.g., "/cart/:id").
     * If a match is found, it extracts the dynamic path parameters and stores them in the
     * provided pathParams map.
     *
     * @param routePattern The registered route pattern to match against (e.g., "/a/:id/b").
     * @param requestPath  The actual request path from the incoming HTTP request (e.g.,
     *                     "/a/123/b").
     * @param pathParams   A map to store extracted path parameters if a match is found.
     * @return true if the request path matches the route pattern; false otherwise.
     */
    private boolean isMatch(String routePattern, String requestPath,
                            Map<String, String> pathParams) {
        String[] patternSegments = routePattern.split("/");
        String[] requestSegments = requestPath.split("/");

        if (patternSegments.length != requestSegments.length) {
            return false;
        }

        // Create a temporary map to hold variables for this specific route evaluation.
        // We do this to prevent polluting the global pathParams map on partial matching routes.
        Map<String, String> tempParams = new HashMap<>();

        for (int i = 0; i < patternSegments.length; i++) {
            String pattern = patternSegments[i];
            String request = requestSegments[i];

            if (pattern.startsWith(":")) {
                String paramName = pattern.substring(1);
                tempParams.put(paramName, request);
            } else if (!pattern.equals(request)) {
                return false;
            }
        }

        pathParams.putAll(tempParams);
        return true;
    }

    /**
     * Starts the HTTP server and listens for incoming client connections.
     *
     * @throws IOException if an I/O error occurs when waiting for a connection or when handling
     * client requests.
     */
    public void start() throws IOException {
        System.out.println("HTTP Server started on port " + serverSocket.getLocalPort());

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    /**
     * Handles an individual client connection by reading the incoming HTTP request, dispatching
     * it to the appropriate handler,
     * and sending back the HTTP response.
     *
     * @param clientSocket The socket representing the client connection.
     */
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