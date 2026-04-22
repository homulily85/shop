package com.shop.controller;

import com.shop.dto.CartItemDTO;
import com.shop.service.CartService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;
import tools.jackson.databind.ObjectMapper;

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

                        cartService.removeItem(pathParams.get("id"), objectMapper.readValue(body,
                                Long.class));

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
