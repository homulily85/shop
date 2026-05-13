package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.OrderService;
import com.shop.webserver.HttpTextResponse;
import com.shop.webserver.HttpServer;

import java.util.Map;

public class OrderController extends AbstractController {
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderController(HttpServer server) {
        super(server);
        this.orderService = OrderService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/orders/:id", ((method, queryParams, pathParams, headers, body) -> {
            if (!method.equals("GET")) {
                return new HttpTextResponse(405, "Method Not Allowed", null);
            }

            var order = orderService.getOrderById(pathParams.get("id"));
            if (order == null) {
                return new HttpTextResponse(404, "Not Found", objectMapper.writeValueAsString(Map.of(
                        "error", "Order not found")));
            }

            return new HttpTextResponse(200, "OK", objectMapper.writeValueAsString(order));
        }));

        server.addRoute("/checkout/:id", ((method, queryParams, pathParams, headers, body) -> {
            if (!method.equals("POST")) {
                return new HttpTextResponse(405, "Method Not Allowed", null);
            }

            var customerId = pathParams.get("id");

            var orderId = orderService.checkout(customerId);

            return new HttpTextResponse(200, "OK", objectMapper.writeValueAsString(Map.of(
                    "orderId", orderId)));
        }));

        server.addRoute("/bill/:id/payment-result", ((method, queryParams, pathParams, headers,
                body) -> {
            if (!method.equals("POST")) {
                return new HttpTextResponse(405, "Method Not Allowed", null);
            }

            var requestBodyJson = objectMapper.readTree(body);

            if (!requestBodyJson.has("success")) {
                return new HttpTextResponse(400, "Bad Request", objectMapper.writeValueAsString(Map.of(
                        "error", "Missing 'success' field in request body")));
            }

            orderService.handleCallback(pathParams.get("id"),
                    requestBodyJson.get("success").asBoolean());

            return new HttpTextResponse(200, "OK", null);
        }));

    }
}
