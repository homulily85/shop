package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.CartItemDTO;
import com.shop.service.CartService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

import java.util.Map;

public class CartController extends AbstractController {
    private final CartService cartService;
    private final ObjectMapper objectMapper;

    public CartController(HttpServer server) {
        super(server);
        this.cartService = CartService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/cart/:id", (method, queryParams, pathParams, headers, body) -> {
            try {
                switch (method) {
                    case "GET" -> {
                        var cartItems = cartService.getCartItems(pathParams.get("id"));
                        return new HttpResponse(200, "OK",
                                objectMapper.writeValueAsString(cartItems));
                    }

                    case "POST" -> {
                        CartItemDTO cartItemDTO = objectMapper.readValue(body, CartItemDTO.class);
                        cartService.addToCart(pathParams.get("id"), cartItemDTO.productId(),
                                cartItemDTO.quantity());
                        return new HttpResponse(200, "OK", null);
                    }

                    case "DELETE" -> {
                        var jsonNode = objectMapper.readTree(body);
                        long productId = jsonNode.get("productId").asLong();
                        cartService.removeItem(pathParams.get("id"), productId);
                        return new HttpResponse(200, "OK", null);
                    }

                    default -> {
                        return new HttpResponse(405, "Method Not Allowed", null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    return new HttpResponse(400, "Bad Request",
                            objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
                } catch (Exception ex) {
                    return new HttpResponse(400, "Bad Request", null);
                }
            }
        });
    }
}