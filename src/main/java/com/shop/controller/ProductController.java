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
            if (method.equals("GET")) {
                if (queryParams.containsKey("status")) {
                    var products = productService.getProductByStatus(queryParams.get("status"));
                    return new HttpResponse(200, "OK", objectMapper.writeValueAsString(products));
                }

                int pageNumber = Integer.parseInt(queryParams.getOrDefault("pageNumber", "0"));
                int pageSize = Integer.parseInt(queryParams.getOrDefault("pageSize", "10"));

                var products = productService.getAllProducts(pageNumber, pageSize);
                return new HttpResponse(200, "OK", objectMapper.writeValueAsString(products));

            } else if (method.equals("POST")) {
                if (body == null || body.length == 0) {
                    throw new IllegalArgumentException("Missing JSON body");
                }

                var newProduct = productService.createAProduct(objectMapper.readValue(body,
                        ProductDTO.class));
                if (newProduct == null) {
                    throw new RuntimeException("Failed to create product.");
                }

                return new HttpResponse(201, "Created",
                        objectMapper.writeValueAsString(newProduct));
            }

            return new HttpResponse(405, "Method Not Allowed", "");
        });

        server.addRoute("/products/:id", (method, queryParams, pathParams, headers, body) -> {
            switch (method) {
                case "GET" -> {
                    var product = productService.getProductById(pathParams.get("id"));
                    if (product == null) {
                        return new HttpResponse(404, "Not Found",
                                objectMapper.writeValueAsString(Map.of("message", "Product not " +
                                        "found.")));
                    }
                    return new HttpResponse(200, "OK", objectMapper.writeValueAsString(product));
                }
                case "DELETE" -> {
                    productService.deleteAProduct(Long.parseLong(pathParams.get("id")));
                    return new HttpResponse(200, "OK", null);
                }
                case "PATCH" -> {
                    if (body == null || body.length == 0) {
                        throw new IllegalArgumentException("Missing JSON body");
                    }

                    var existingProduct = productService.getProductById(pathParams.get("id"));
                    if (existingProduct == null) {
                        return new HttpResponse(404, "Not Found",
                                objectMapper.writeValueAsString(Map.of("message", "Product not " +
                                        "found.")));
                    }

                    var jsonNode = objectMapper.readTree(body);

                    String updatedTitle = jsonNode.has("title") ? jsonNode.get("title").asText()
                            : existingProduct.title();
                    long updatedPrice = jsonNode.has("price") ? jsonNode.get("price").asLong() :
                            existingProduct.price();
                    String updatedDescription = jsonNode.has("description") ? jsonNode.get(
                            "description").asText() : existingProduct.description();
                    long updatedQuantity = jsonNode.has("availableQuantity") ? jsonNode.get(
                            "availableQuantity").asLong() : existingProduct.availableQuantity();
                    String updatedCategory = jsonNode.has("category") ?
                            jsonNode.get("category").asText() : existingProduct.category();
                    String updatedStatus = jsonNode.has("status") ?
                            jsonNode.get("status").asText() : existingProduct.status();
                    String updatedImageLink = jsonNode.has("imageLink") ? jsonNode.get("imageLink"
                    ).asText() : existingProduct.imageLink();

                    var productTobeUpdatedWithId = new Product(
                            existingProduct.id(),
                            updatedTitle,
                            updatedPrice,
                            updatedDescription,
                            updatedQuantity,
                            updatedCategory,
                            updatedStatus,
                            updatedImageLink
                    );

                    var updatedProduct = productService.updateAProduct(productTobeUpdatedWithId);
                    if (updatedProduct == null) {
                        throw new RuntimeException("Failed to update product.");
                    }
                    return new HttpResponse(200, "OK",
                            objectMapper.writeValueAsString(updatedProduct));
                }
                default -> {
                    return new HttpResponse(405, "Method Not Allowed", null);
                }
            }
        });
    }
}