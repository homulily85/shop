package com.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.controller.CartController;
import com.shop.controller.ProductController;
import com.shop.controller.UploadController;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        var server = new HttpServer(Integer.parseInt(System.getenv("PORT")));
        ObjectMapper mapper = new ObjectMapper();

        server.addRoute("/test",
                (method, queryParams, pathParams, headers, body) -> {
                    try {
                        return new HttpResponse(200, "OK", mapper.writeValueAsString(Map.of("message", "Hello World!")));
                    } catch (Exception e) {
                        return new HttpResponse(500, "Internal Server Error", "");
                    }
                });

        ProductController productController = new ProductController(server);
        productController.registerRoutes();

        CartController cartController = new CartController(server);
        cartController.registerRoutes();

        UploadController uploadController = new UploadController(server);
        uploadController.registerRoutes();

        server.start();
    }
}