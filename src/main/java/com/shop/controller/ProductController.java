package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.ProductDTO;
import com.shop.model.Product;
import com.shop.service.ProductService;
import com.shop.service.RateLimiterService;
import com.shop.webserver.HttpTextResponse;
import com.shop.webserver.HttpServer;

import java.util.Map;

public class ProductController extends AbstractController {

    private final ProductService productService;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public ProductController(HttpServer server) {
        super(server);
        this.productService = ProductService.getInstance();
        this.rateLimiterService = RateLimiterService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/products", (method, queryParams, pathParams, headers, body) -> {
            if (rateLimiterService.isAllowed(headers.getOrDefault("X-Real-Ip", "unknown-client"))){
                return errorResponse(429, "Too Many Requests", "You are requesting too fast.");
            }

            if (method.equals("GET")) {
                if (queryParams.containsKey("status")) {
                    var products = productService.getProductByStatus(queryParams.get("status"));
                    return new HttpTextResponse(200, "OK", objectMapper.writeValueAsString(products));
                }

                int pageNumber = 0;
                int pageSize = 10;

                if (queryParams.containsKey("pageNumber")) {
                    try {
                        pageNumber = Integer.parseInt(queryParams.get("pageNumber"));
                    } catch (NumberFormatException e) {
                        return errorResponse(400, "Bad Request", "Invalid pageNumber");
                    }
                }

                if (queryParams.containsKey("pageSize")) {
                    try {
                        pageSize = Integer.parseInt(queryParams.get("pageSize"));
                    } catch (NumberFormatException e) {
                        return errorResponse(400, "Bad Request", "Invalid pageSize");
                    }
                }

                if (pageNumber < 0) {
                    return errorResponse(400, "Bad Request", "pageNumber must be >= 0");
                }
                if (pageSize <= 0) {
                    return errorResponse(400, "Bad Request", "pageSize must be > 0");
                }

                String sortBy = queryParams.getOrDefault("sortBy", "id");
                String sortOrder = queryParams.getOrDefault("sortOrder", "asc");

                if (sortBy.isBlank()) {
                    return errorResponse(400, "Bad Request", "sortBy must not be blank");
                }

                if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
                    return errorResponse(400, "Bad Request", "sortOrder must be 'asc' or 'desc'");
                }

                var products = productService.getAllProducts(pageNumber, pageSize, sortBy,
                        sortOrder);

                long totalItems = productService.getTotalProductCount();
                int totalPages = (int) Math.ceil((double) totalItems / pageSize);
                int pagesLeft = Math.max(0, totalPages - (pageNumber + 1));

                var responseBody = Map.of(
                        "data", products,
                        "pagination", Map.of(
                                "pageNumber", pageNumber,
                                "pageSize", pageSize,
                                "totalItems", totalItems,
                                "totalPages", totalPages,
                                "pagesLeft", pagesLeft
                        ),
                        "sorting", Map.of(
                                "sortBy", sortBy,
                                "sortOrder", sortOrder
                        )
                );

                return new HttpTextResponse(200, "OK", objectMapper.writeValueAsString(responseBody));
            } else if (method.equals("POST")) {
                if (body == null || body.length == 0) {
                    throw new IllegalArgumentException("Missing JSON body");
                }

                var newProduct = productService.createAProduct(objectMapper.readValue(body,
                        ProductDTO.class));
                if (newProduct == null) {
                    throw new RuntimeException("Failed to create product.");
                }

                return new HttpTextResponse(201, "Created",
                        objectMapper.writeValueAsString(newProduct));
            }

            return errorResponse(405, "Method Not Allowed", "Method not allowed");
        });

        server.addRoute("/products/:id", (method, queryParams, pathParams, headers, body) -> {
            switch (method) {
                case "GET" -> {
                    var product = productService.getProductById(pathParams.get("id"));
                    if (product == null) {
                        return errorResponse(404, "Not Found", "Product not found");
                    }
                    return new HttpTextResponse(200, "OK", objectMapper.writeValueAsString(product));
                }
                case "DELETE" -> {
                    productService.deleteAProduct(Long.parseLong(pathParams.get("id")));
                    return new HttpTextResponse(200, "OK", null);
                }
                case "PATCH" -> {
                    if (body == null || body.length == 0) {
                        throw new IllegalArgumentException("Missing JSON body");
                    }

                    var existingProduct = productService.getProductById(pathParams.get("id"));
                    if (existingProduct == null) {
                        return new HttpTextResponse(404, "Not Found",
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

                        if (jsonNode.has("title") && updatedTitle.isBlank()) {
                        throw new IllegalArgumentException("title must not be blank");
                        }
                        if (jsonNode.has("price") && updatedPrice < 0) {
                        throw new IllegalArgumentException("price must be >= 0");
                        }
                        if (jsonNode.has("availableQuantity") && updatedQuantity < 0) {
                        throw new IllegalArgumentException("availableQuantity must be >= 0");
                        }
                        if (jsonNode.has("category") && updatedCategory.isBlank()) {
                        throw new IllegalArgumentException("category must not be blank");
                        }
                        if (jsonNode.has("status") && updatedStatus.isBlank()) {
                        throw new IllegalArgumentException("status must not be blank");
                        }

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
                    return new HttpTextResponse(200, "OK",
                            objectMapper.writeValueAsString(updatedProduct));
                }
                default -> {
                    return errorResponse(405, "Method Not Allowed", "Method not allowed");
                }
            }
        });
    }

    private HttpTextResponse errorResponse(int statusCode, String statusMessage, String error)
            throws Exception {
        return new HttpTextResponse(statusCode, statusMessage,
                objectMapper.writeValueAsString(Map.of("error", error)));
    }
}