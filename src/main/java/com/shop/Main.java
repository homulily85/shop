package com.shop;

import com.shop.controller.CartController;
import com.shop.controller.ProductController;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var server = new HttpServer(Integer.parseInt(System.getenv("PORT")));

        server.addRoute("/test",
                (method, queryParams, pathParams, headers, body) ->
                        new HttpResponse(200, "OK", "Hello World!"));

        ProductController productController = new ProductController(server);
        productController.registerRoutes();

        CartController cartController = new CartController(server);
        cartController.registerRoutes();

        server.start();
    }
}