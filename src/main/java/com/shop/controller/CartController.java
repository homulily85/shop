package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.CartItemDTO;
import com.shop.service.CartService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

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
            switch (method) {
                case "GET" -> {
                    var cartItems = cartService.getCart(pathParams.get("id"));
                    if (cartItems == null) {
                        return new HttpResponse(200, "OK", null);
                    }
                    return new HttpResponse(200, "OK", objectMapper.writeValueAsString(cartItems));
                }

                case "POST" -> {
                    if (body == null || body.length == 0) {
                        throw new IllegalArgumentException("Missing JSON body");
                    }
                    CartItemDTO cartItemDTO = objectMapper.readValue(body, CartItemDTO.class);
                    cartService.updateCart(pathParams.get("id"), cartItemDTO.productId(),
                            cartItemDTO.quantity());

                    return new HttpResponse(200, "OK", null);
                }

                case "DELETE" -> {
                    if (body == null || body.length == 0) {
                        throw new IllegalArgumentException("Missing JSON body");
                    }
                    var jsonNode = objectMapper.readTree(body);
                    long productId = jsonNode.get("productId").asLong();
                    cartService.removeItem(pathParams.get("id"), productId);

                    return new HttpResponse(200, "OK", null);
                }

                default -> {
                    return new HttpResponse(405, "Method Not Allowed", null);
                }
            }
        });
    }
}