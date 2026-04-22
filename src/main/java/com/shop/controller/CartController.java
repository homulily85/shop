package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.CartItemDTO;
import com.shop.service.CartService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

public class CartController extends AbstractController {
    private final CartService cartService;

    public CartController(HttpServer server) {
        super(server);

        this.cartService = CartService.getInstance();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/cart/:id", (method, queryParams, pathParams, headers, body) -> {
            try {
                switch (method) {
                    case "GET" -> {
                        return new HttpResponse(200, "OK", cartService.getCartItems(pathParams.get(
                                "id")));
                    }

                    case "POST" -> {
                        var objectMapper = new ObjectMapper();

                        CartItemDTO cartItemDTO = objectMapper.readValue(body, CartItemDTO.class);

                        cartService.addToCart(pathParams.get("id"), cartItemDTO.productId(),
                                cartItemDTO.quantity());

                        return new HttpResponse(200, "OK", "{\"message\": \"Item added to cart\"}");
                    }

                    case "DELETE" -> {
                        var objectMapper = new ObjectMapper();

                        var jsonNode = objectMapper.readTree(body);
                        long productId = jsonNode.get("productId").asLong();
                        cartService.removeItem(pathParams.get("id"), productId);

                        return new HttpResponse(200, "OK", "{\"message\": \"Item removed from " +
                                "cart\"}");
                    }

                    default -> {
                        return new HttpResponse(405, "Method Not Allowed", "");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new HttpResponse(400, "Bad Request",
                        "{\"error\": \"%s\"}".formatted(e.getMessage()));
            }
        });
    }
}
