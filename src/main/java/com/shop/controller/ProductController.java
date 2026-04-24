package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.ProductDTO;
import com.shop.model.Product;
import com.shop.service.ProductService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

import java.util.Map;

public class ProductController extends AbstractController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public ProductController(HttpServer server) {
        super(server);
        this.productService = ProductService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/products", (method, queryParams, pathParams, headers, body) -> {
            try {
                if (method.equals("GET")) {
                    if (queryParams.containsKey("status")) {
                        var products = productService.getProductByStatus(queryParams.get("status"));
                        return new HttpResponse(200, "OK",
                                objectMapper.writeValueAsString(products));
                    }

                    int pageNumber = Integer.parseInt(queryParams.getOrDefault("pageNumber", "0"));
                    int pageSize = Integer.parseInt(queryParams.getOrDefault("pageSize", "10"));

                    var products = productService.getAllProducts(pageNumber, pageSize);
                    return new HttpResponse(200, "OK", objectMapper.writeValueAsString(products));

                } else if (method.equals("POST")) {
                    if (body == null || body.length == 0) {
                        return new HttpResponse(400, "Bad Request",
                                objectMapper.writeValueAsString(Map.of("error", "Missing JSON " +
                                        "body")));
                    }

                    var newProduct = productService.createAProduct(objectMapper.readValue(body,
                            ProductDTO.class));
                    if (newProduct == null) {
                        return new HttpResponse(500, "Internal Server Error",
                                objectMapper.writeValueAsString(Map.of("message", "Failed to " +
                                        "create product.")));
                    }

                    return new HttpResponse(201, "Created",
                            objectMapper.writeValueAsString(newProduct));
                }

                return new HttpResponse(405, "Method Not Allowed", "");
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

        server.addRoute("/products/:id", (method, queryParams, pathParams, headers, body) -> {
            try {
                switch (method) {
                    case "GET" -> {
                        var product = productService.getProductById(pathParams.get("id"));
                        if (product == null) {
                            return new HttpResponse(404, "Not Found",
                                    objectMapper.writeValueAsString(Map.of("message", "Product " +
                                            "not found.")));
                        }
                        return new HttpResponse(200, "OK",
                                objectMapper.writeValueAsString(product));
                    }
                    case "DELETE" -> {
                        productService.deleteAProduct(Long.parseLong(pathParams.get("id")));
                        return new HttpResponse(200, "OK",
                                objectMapper.writeValueAsString(Map.of("message", "Product " +
                                        "deleted")));
                    }
                    case "PUT" -> {
                        if (body == null || body.length == 0) {
                            return new HttpResponse(400, "Bad Request",
                                    objectMapper.writeValueAsString(Map.of("error", "Missing JSON body")));
                        }

                        var productTobeUpdated = objectMapper.readValue(body, ProductDTO.class);

                        var productTobeUpdatedWithId = new Product(
                                Long.parseLong(pathParams.get("id")),
                                productTobeUpdated.title(),
                                productTobeUpdated.price(),
                                productTobeUpdated.description(),
                                productTobeUpdated.quantity(),
                                productTobeUpdated.category(),
                                productTobeUpdated.status(),
                                productTobeUpdated.imageLink()
                        );

                        var updatedProduct =
                                productService.updateAProduct(productTobeUpdatedWithId);

                        if (updatedProduct == null) {
                            return new HttpResponse(500, "Internal Server Error",
                                    objectMapper.writeValueAsString(Map.of("message", "Failed to " +
                                            "update product.")));
                        }

                        return new HttpResponse(200, "OK",
                                objectMapper.writeValueAsString(updatedProduct));
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