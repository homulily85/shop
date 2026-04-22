package com.shop.controller;

import com.shop.service.CartService;
import com.shop.utility.MinimalJsonParser;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

import java.util.Map;

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
                        var parser = new MinimalJsonParser(body);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> jsonBody = (Map<String, Object>) parser.parse();

                        long productId = ((Number) jsonBody.get("productId")).longValue();
                        long quantity = ((Number) jsonBody.get("quantity")).longValue();

                        cartService.addToCart(pathParams.get("id"), productId, quantity);

                        return new HttpResponse(200, "OK", "{\"message\": \"Item added to cart\"}");
                    }
                    case "DELETE" -> {
                        var parser = new MinimalJsonParser(body);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> jsonBody = (Map<String, Object>) parser.parse();

                        long productId = ((Number) jsonBody.get("productId")).longValue();

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
