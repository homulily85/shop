package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.CartItemDTO;
import com.shop.service.CartService;
import com.shop.service.RateLimiterService;
import com.shop.webserver.HttpServer;
import com.shop.webserver.HttpTextResponse;

public class CartController extends AbstractController {
    private final CartService cartService;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public CartController(HttpServer server) {
        super(server);
        this.cartService = CartService.getInstance();
        this.rateLimiterService = RateLimiterService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/cart/:id", (method, queryParams, pathParams, headers, body) -> {
            String cartId = pathParams.get("id");

            if (rateLimiterService.isAllowed(headers.getOrDefault(cartId != null ? cartId : -1,
                    "unknown-client"))) {
                return errorResponse(429, "Too Many Requests", "You are requesting too fast.");
            }

            if (cartId == null || cartId.isBlank()) {
                return errorResponse(400, "Bad Request", "Missing cart id");
            }

            switch (method) {
                case "GET" -> {
                    var cartItems = cartService.getCart(cartId);
                    if (cartItems == null) {
                        return new HttpTextResponse(200, "OK", null);
                    }
                    return new HttpTextResponse(200, "OK",
                            objectMapper.writeValueAsString(cartItems));
                }

                case "POST" -> {
                    if (body == null || body.length == 0) {
                        throw new IllegalArgumentException("Missing JSON body");
                    }
                    CartItemDTO cartItemDTO = objectMapper.readValue(body, CartItemDTO.class);
                    if (cartItemDTO.productId() <= 0 || cartItemDTO.quantity() <= 0) {
                        throw new IllegalArgumentException("Invalid productId or quantity");
                    }
                    cartService.updateCart(cartId, cartItemDTO.productId(), cartItemDTO.quantity());

                    return new HttpTextResponse(200, "OK", null);
                }

                case "DELETE" -> {
                    if (body == null || body.length == 0) {
                        throw new IllegalArgumentException("Missing JSON body");
                    }
                    var jsonNode = objectMapper.readTree(body);
                    if (!jsonNode.has("productId")) {
                        throw new IllegalArgumentException("Missing productId in request body");
                    }
                    long productId = jsonNode.get("productId").asLong();
                    if (productId <= 0) {
                        throw new IllegalArgumentException("Invalid productId");
                    }
                    cartService.removeItem(cartId, productId);

                    return new HttpTextResponse(200, "OK", null);
                }

                default -> {
                    return errorResponse(405, "Method Not Allowed", "Method not allowed");
                }
            }
        });
    }

    private HttpTextResponse errorResponse(int statusCode, String statusMessage, String error)
            throws Exception {
        return new HttpTextResponse(statusCode, statusMessage,
                objectMapper.writeValueAsString(java.util.Map.of("error", error)));
    }
}