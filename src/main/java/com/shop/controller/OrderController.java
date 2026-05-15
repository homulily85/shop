package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.OrderService;
import com.shop.service.RateLimiterService;
import com.shop.webserver.HttpServer;
import com.shop.webserver.HttpTextResponse;

import java.util.Map;

public class OrderController extends AbstractController {
    private final OrderService orderService;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public OrderController(HttpServer server) {
        super(server);
        this.orderService = OrderService.getInstance();
        this.rateLimiterService = RateLimiterService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/orders/:id", ((method, queryParams, pathParams, headers, body) -> {
            String orderId = pathParams.get("id");
            if (rateLimiterService.isAllowed(headers.getOrDefault(orderId != null ? orderId : -1,
                    "unknown-client"))) {
                return errorResponse(429, "Too Many Requests", "You are requesting too fast.");
            }

            if (!method.equals("GET")) {
                return errorResponse(405, "Method Not Allowed", "Method not allowed");
            }

            if (orderId == null || orderId.isBlank()) {
                return errorResponse(400, "Bad Request", "Missing order id");
            }

            var order = orderService.getOrderById(orderId);
            if (order == null) {
                return errorResponse(404, "Not Found", "Order not found");
            }

            return new HttpTextResponse(200, "OK", objectMapper.writeValueAsString(order));
        }));

        server.addRoute("/checkout/:id", ((method, queryParams, pathParams, headers, body) -> {
            if (!method.equals("POST")) {
                return errorResponse(405, "Method Not Allowed", "Method not allowed");
            }

            var customerId = pathParams.get("id");
            if (customerId == null || customerId.isBlank()) {
                return errorResponse(400, "Bad Request", "Missing customer id");
            }

            var orderId = orderService.checkout(customerId);

            return new HttpTextResponse(200, "OK", objectMapper.writeValueAsString(Map.of(
                    "orderId", orderId)));
        }));

        server.addRoute("/bill/:id/payment-result", ((method, queryParams, pathParams, headers,
                                                      body) -> {
            if (!method.equals("POST")) {
                return errorResponse(405, "Method Not Allowed", "Method not allowed");
            }

            String billId = pathParams.get("id");
            if (billId == null || billId.isBlank()) {
                return errorResponse(400, "Bad Request", "Missing bill id");
            }

            if (body == null || body.length == 0) {
                throw new IllegalArgumentException("Missing JSON body");
            }

            var requestBodyJson = objectMapper.readTree(body);

            if (!requestBodyJson.has("success")) {
                return errorResponse(400, "Bad Request", "Missing 'success' field in request body");
            }

            if (!requestBodyJson.get("success").isBoolean()) {
                return errorResponse(400, "Bad Request", "Field 'success' must be boolean");
            }

            orderService.handleCallback(billId, requestBodyJson.get("success").asBoolean());

            return new HttpTextResponse(200, "OK", null);
        }));

    }

    private HttpTextResponse errorResponse(int statusCode, String statusMessage, String error)
            throws Exception {
        return new HttpTextResponse(statusCode, statusMessage,
                objectMapper.writeValueAsString(Map.of("error", error)));
    }
}
