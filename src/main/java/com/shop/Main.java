package com.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.controller.CartController;
import com.shop.controller.OrderController;
import com.shop.controller.ProductController;
import com.shop.controller.ImageStorageController;
import com.shop.scheduler.JobScheduler;
import com.shop.webserver.HttpTextResponse;
import com.shop.webserver.HttpServer;

import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        var server = new HttpServer(Integer.parseInt(System.getenv("PORT")));
        ObjectMapper mapper = new ObjectMapper();

        JobScheduler jobScheduler = new JobScheduler();
        jobScheduler.startJobs();
        Runtime.getRuntime().addShutdownHook(new Thread(jobScheduler::shutdown));

        server.addRoute("/test",
                (method, queryParams, pathParams, headers, body) -> {
                    try {
                        return new HttpTextResponse(200, "OK", mapper.writeValueAsString(Map.of(
                                "message", "Hello World!")));
                    } catch (Exception e) {
                        return new HttpTextResponse(500, "Internal Server Error", "");
                    }
                });

        ProductController productController = new ProductController(server);
        productController.registerRoutes();

        CartController cartController = new CartController(server);
        cartController.registerRoutes();

        ImageStorageController imageStorageController = new ImageStorageController(server);
        imageStorageController.registerRoutes();

        OrderController orderController = new OrderController(server);
        orderController.registerRoutes();

        server.start();
    }
}