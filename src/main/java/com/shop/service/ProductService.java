package com.shop.service;

import com.shop.model.Product;
import com.shop.redis.Client;
import com.shop.repository.ProductRepository;
import redis.clients.jedis.RedisClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductService {
    private static final String POPULAR_HASH_KEY = "product:popular";
    private final ProductRepository productRepository = ProductRepository.getInstance();
    private final RedisClient redisClient = Client.getRedisClient();

    private ProductService() {
    }

    public static ProductService getInstance() {
        return Holder.INSTANCE;
    }

    public String getAllProducts(int pageNumber, int pageSize) {
        var products = productRepository.getAllProducts(pageNumber, pageSize);

        return toJson(products);
    }

    private String getPopularProducts() {
        if (redisClient.exists(POPULAR_HASH_KEY)) {
            return redisClient.hvals(POPULAR_HASH_KEY).stream().reduce("[\n",
                    (acc, product) -> acc + product + ",\n", String::concat).replaceAll(",\n$",
                    "\n]");
        }

        Map<String, String> popularProductsMap = new HashMap<>();
        var popularProducts = productRepository.getProductByStatus("popular");
        for (Product product : popularProducts) {
            popularProductsMap.put(String.valueOf(product.getId()), product.toString());
        }

        if (!popularProductsMap.isEmpty()) {
            redisClient.hset(POPULAR_HASH_KEY, popularProductsMap);
            redisClient.expire(POPULAR_HASH_KEY, 3600);
        }

        return toJson(popularProducts);
    }

    public String getProductByStatus(String status) {
        if (status.equalsIgnoreCase("popular")) {
            return getPopularProducts();
        }

        var products = productRepository.getProductByStatus(status);

        return toJson(products);
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

        return product.toString();
    }

    public String createAProduct(Product product) {
        var createdProduct = productRepository.createNewProduct(product);

        if (createdProduct == null) {
            return """
                    {
                        "message": "Failed to create product."
                    }
                    """;
        }

        if (redisClient.exists(POPULAR_HASH_KEY)) {
            if ("popular".equalsIgnoreCase(createdProduct.getStatus())) {
                redisClient.hset(POPULAR_HASH_KEY, String.valueOf(createdProduct.getId()),
                        createdProduct.toString());
            }
        }

        return createdProduct.toString();
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

        if (redisClient.exists(POPULAR_HASH_KEY)) {
            if ("popular".equalsIgnoreCase(product.getStatus())) {
                redisClient.hset(POPULAR_HASH_KEY, String.valueOf(product.getId()),
                        product.toString());
            } else {
                redisClient.hdel(POPULAR_HASH_KEY, String.valueOf(product.getId()));
            }
        }

        return product.toString();
    }

    public String deleteAProduct(long productId) {
        productRepository.deleteAProduct(productId);
        redisClient.hdel(POPULAR_HASH_KEY, String.valueOf(productId));
        return "";
    }

    private String toJson(List<Product> products) {
        StringBuilder response = new StringBuilder("[\n");

        for (int i = 0; i < products.size(); i++) {
            response.append(products.get(i));
            if (i != products.size() - 1) {
                response.append(",\n");
            } else {
                response.append("\n]");
            }
        }
        return response.toString();
    }

    private static class Holder {
        private static final ProductService INSTANCE = new ProductService();
    }

}