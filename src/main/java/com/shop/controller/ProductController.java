package com.shop.controller;

import com.shop.model.Product;
import com.shop.service.ProductService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

public class ProductController {

    private final HttpServer server;
    private final ProductService productService;

    public ProductController(HttpServer server) {
        this.server = server;
        this.productService = ProductService.getInstance();
    }

    public void registerRoutes() {
        server.addRoute("/products", (method, queryParams, pathParams, headers, body) -> {
            if (method.equals("GET")) {
                if (queryParams.containsKey("status")) {
                    return new HttpResponse(200, "OK",
                            productService.getProductByStatus(queryParams.get("status")));
                }

                return new HttpResponse(200, "OK",
                        productService.getAllProducts(Integer.parseInt(headers.getOrDefault(
                                "pageNumber", "0")), Integer.parseInt(headers.getOrDefault(
                                "pageSize", "10"))));
            } else if (method.equals("POST")) {
                try {
                    if (body == null || body.isBlank()) {
                        return new HttpResponse(400, "Bad Request",
                                "{\"error\": \"Missing JSON " + "body\"}");
                    }

                    var newProduct = productService.createAProduct(Product.parseProduct(body));
                    return new HttpResponse(201, "Created", newProduct);

                } catch (Exception e) {
                    e.printStackTrace();
                    return new HttpResponse(400, "Bad Request",
                            "{\"error\": \"%s\"}".formatted(e.getMessage()));
                }
            }

            return new HttpResponse(405, "Method Not Allowed", "");
        });

        server.addRoute("/products/:id", (method, queryParams, pathParams, headers, body) -> {
            switch (method) {
                case "GET" -> {
                    return new HttpResponse(200, "OK",
                            productService.getProductById(pathParams.get("id")));
                }
                case "DELETE" -> {
                    return new HttpResponse(200, "OK",
                            productService.deleteAProduct(Long.parseLong(pathParams.get("id"))));
                }
                case "PUT" -> {
                    try {
                        if (body == null || body.isBlank()) {
                            return new HttpResponse(400, "Bad Request",
                                    "{\"error\": \"Missing " + "JSON body\"}");
                        }

                        var productTobeUpdated = Product.parseProduct(body);
                        var productTobeUpdatedWithId = new Product(Long.parseLong(pathParams.get(
                                "id")), productTobeUpdated.getTitle(),
                                productTobeUpdated.getPrice(),
                                productTobeUpdated.getDescription(),
                                productTobeUpdated.getQuantity(),
                                productTobeUpdated.getCategory(), productTobeUpdated.getStatus(),
                                productTobeUpdated.getImageLink());

                        var updatedProduct =
                                productService.updateAProduct(productTobeUpdatedWithId);

                        return new HttpResponse(200, "OK", updatedProduct);

                    } catch (Exception e) {
                        e.printStackTrace();
                        return new HttpResponse(400, "Bad Request",
                                "{\"error\": \"%s\"}".formatted(e.getMessage()));
                    }
                }
            }

            return new HttpResponse(405, "Method Not Allowed", "");
        });
    }
}