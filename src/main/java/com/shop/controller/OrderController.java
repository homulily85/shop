package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.OrderService;
import com.shop.webserver.HttpResponse;
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
                return new HttpResponse(405, "Method Not Allowed", null);
            }

            var order = orderService.getOrderById(pathParams.get("id"));
            if (order == null) {
                return new HttpResponse(404, "Not Found", objectMapper.writeValueAsString(Map.of(
                        "error", "Order not found")));
            }

            return new HttpResponse(200, "OK", objectMapper.writeValueAsString(order));
        }));

        server.addRoute("/checkout", ((method, queryParams, pathParams, headers, body) -> {
            if (!method.equals("POST")) {
                return new HttpResponse(405, "Method Not Allowed", null);
            }

            if (body == null || body.length == 0) {
                return new HttpResponse(400, "Bad Request", objectMapper.writeValueAsString(Map.of(
                        "error", "Missing JSON body")));
            }

            var jsonNode = objectMapper.readTree(body);
            String cartId = jsonNode.get("cartId").asText();
            long customerId = jsonNode.get("customerId").asLong();

            orderService.checkout(cartId, customerId);

            return new HttpResponse(200, "OK", objectMapper.writeValueAsString(Map.of(
                    "message", "Checkout successful")));
        }));
    }
}
