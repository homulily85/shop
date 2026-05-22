package com.shop.webserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

public class HttpServer {
    private final Map<String, RequestHandler> routes;
    private final ServerSocketChannel serverChannel;

    public HttpServer(int port) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.bind(new InetSocketAddress(port));
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
            try {
                byte[] decodedBody = new byte[0];
                if (httpRequest.body() != null && !httpRequest.body().isBlank()) {
                    decodedBody = Base64.getDecoder().decode(httpRequest.body());
                }

                return handler.handle(httpRequest.method(),
                        httpRequest.query(),
                        pathParams,
                        httpRequest.headers(),
                        decodedBody);

            } catch (IllegalArgumentException | JsonProcessingException e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Invalid request data";
                return errorResponse(400, "Bad Request", errorMsg, mapper);
            } catch (Exception e) {
                e.printStackTrace();
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
                return errorResponse(500, "Internal Server Error", errorMsg, mapper);
            }
        } else {
            return errorResponse(404, "Not Found", "Route not found.", mapper);
        }
    }

    private HttpTextResponse errorResponse(int statusCode, String statusMessage, String error,
                                           ObjectMapper mapper) {
        try {
            return new HttpTextResponse(statusCode, statusMessage,
                    mapper.writeValueAsString(Map.of("error", error)));
        } catch (Exception e) {
            return new HttpTextResponse(statusCode, statusMessage, "");
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
        int port = ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
        System.out.println("HTTP Server started on port " + port);

        try (Selector selector = Selector.open()) {
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        acceptClient(selector);
                    } else if (key.isReadable()) {
                        readClientRequest(key);
                    } else if (key.isWritable()) {
                        writeClientResponse(key);
                    }
                }
            }
        }
    }

    private void acceptClient(Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) {
            return;
        }

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ, new ClientState());
    }

    private void readClientRequest(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientState state = (ClientState) key.attachment();

        try {
            int bytesRead = channel.read(state.readBuffer);
            if (bytesRead == -1) {
                if (state.inbound.length() == 0) {
                    closeChannel(key, channel);
                } else {
                    String requestLine = state.inbound.toString().trim();
                    state.inbound.setLength(0);
                    processRequestLine(requestLine, key, channel, state);
                }
                return;
            }

            state.readBuffer.flip();
            state.inbound.append(StandardCharsets.UTF_8.decode(state.readBuffer));
            state.readBuffer.clear();

            int newlineIndex = state.inbound.indexOf("\n");
            if (newlineIndex == -1) {
                return;
            }

            String requestLine = state.inbound.substring(0, newlineIndex).trim();
            state.inbound.delete(0, newlineIndex + 1);
            processRequestLine(requestLine, key, channel, state);
        } catch (Exception e) {
            e.printStackTrace();
            closeChannel(key, channel);
        }
    }

    private void writeClientResponse(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientState state = (ClientState) key.attachment();

        try {
            if (state.outbound != null) {
                channel.write(state.outbound);
            }

            if (state.outbound == null || !state.outbound.hasRemaining()) {
                closeChannel(key, channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeChannel(key, channel);
        }
    }

    private void closeChannel(SelectionKey key, SocketChannel channel) {
        try {
            key.cancel();
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequestLine(String requestLine, SelectionKey key,
                                    SocketChannel channel, ClientState state) throws IOException {
        if (requestLine.isEmpty()) {
            closeChannel(key, channel);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        HttpResponse responseData;
        try {
            HttpRequest httpRequest = objectMapper.readValue(requestLine, HttpRequest.class);
            responseData = dispatch(httpRequest);
        } catch (Exception e) {
            responseData = errorResponse(400, "Bad Request",
                    e.getMessage() != null ? e.getMessage() : "Invalid request data",
                    objectMapper);
        }

        String httpResponseString = objectMapper.writeValueAsString(responseData);
        state.outbound = StandardCharsets.UTF_8.encode(httpResponseString);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private static final class ClientState {
        private final StringBuilder inbound = new StringBuilder();
        private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);
        private ByteBuffer outbound;
    }
}