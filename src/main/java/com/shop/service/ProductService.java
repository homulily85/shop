package com.shop.service;

import com.shop.dto.ProductDTO;
import com.shop.model.Product;
import com.shop.redis.Client;
import com.shop.repository.ProductRepository;
import redis.clients.jedis.RedisClient;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ProductService {
    private static final String POPULAR_HASH_KEY = "product:popular";
    private final ProductRepository productRepository = ProductRepository.getInstance();
    private final RedisClient redisClient = Client.getRedisClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductService() {
    }

    public static ProductService getInstance() {
        return Holder.INSTANCE;
    }

    public String getAllProducts(int pageNumber, int pageSize) {
        var products = productRepository.getAllProducts(pageNumber, pageSize);

        return objectMapper.writeValueAsString(products);
    }

    private String getPopularProducts() {
        if (redisClient.exists(POPULAR_HASH_KEY)) {
            var cachedProducts = redisClient.hvals(POPULAR_HASH_KEY);
            return "[" + String.join(",", cachedProducts) + "]";
        }

        Map<String, String> popularProductsMap = new HashMap<>();
        var popularProducts = productRepository.getProductByStatus("popular");

        for (Product product : popularProducts) {
            popularProductsMap.put(String.valueOf(product.id()),
                    objectMapper.writeValueAsString(product));
        }

        if (!popularProductsMap.isEmpty()) {
            redisClient.hset(POPULAR_HASH_KEY, popularProductsMap);
            redisClient.expire(POPULAR_HASH_KEY, 3600);
        }

        return objectMapper.writeValueAsString(popularProducts);
    }

    public String getProductByStatus(String status) {
        if (status.equalsIgnoreCase("popular")) {
            return getPopularProducts();
        }

        var products = productRepository.getProductByStatus(status);

        return objectMapper.writeValueAsString(products);
    }

    public String getProductById(String id) {
        var product = productRepository.getProductById(Long.parseLong(id));

        if (product == null) {
            return """
                    {
                        "message": "Product not found."
                    }
                    """;
        }

        return objectMapper.writeValueAsString(product);
    }

    public String createAProduct(ProductDTO productDTO) {
        var createdProduct = productRepository.createNewProduct(productDTO);

        if (createdProduct == null) {
            return """
                    {
                        "message": "Failed to create product."
                    }
                    """;
        }

        var createdProductJson = objectMapper.writeValueAsString(createdProduct);

        if (redisClient.exists(POPULAR_HASH_KEY)) {
            if ("popular".equalsIgnoreCase(createdProduct.status())) {
                redisClient.hset(POPULAR_HASH_KEY, String.valueOf(createdProduct.id()),
                        createdProductJson);
            }
        }

        return createdProductJson;
    }

    public String updateAProduct(Product product) {
        var updatedProduct = productRepository.updateAProduct(product);
        if (!updatedProduct) {
            return """
                    {
                        "message": "Failed to update product."
                    }
                    """;
        }

        var updatedProductJson = objectMapper.writeValueAsString(product);

        if (redisClient.exists(POPULAR_HASH_KEY)) {
            if ("popular".equalsIgnoreCase(product.status())) {
                redisClient.hset(POPULAR_HASH_KEY, String.valueOf(product.id()),
                        updatedProductJson);
            } else {
                redisClient.hdel(POPULAR_HASH_KEY, String.valueOf(product.id()));
            }
        }

        return updatedProductJson;
    }

    public String deleteAProduct(long productId) {
        productRepository.deleteAProduct(productId);
        redisClient.hdel(POPULAR_HASH_KEY, String.valueOf(productId));
        return "";
    }


    private static class Holder {
        private static final ProductService INSTANCE = new ProductService();
    }

}