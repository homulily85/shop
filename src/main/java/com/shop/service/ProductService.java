package com.shop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.ProductDTO;
import com.shop.model.Product;
import com.shop.redis.Client;
import com.shop.repository.ProductRepository;
import redis.clients.jedis.RedisClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public List<Product> getAllProducts(int pageNumber, int pageSize) {
        return productRepository.getAllProducts(pageNumber, pageSize);
    }

    private List<Product> getPopularProducts() {
        if (redisClient.exists(POPULAR_HASH_KEY)) {
            var cachedProductsJson = redisClient.hvals(POPULAR_HASH_KEY);
            List<Product> products = new ArrayList<>();
            try {
                for (String json : cachedProductsJson) {
                    products.add(objectMapper.readValue(json, Product.class));
                }
                return products;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        Map<String, String> popularProductsMap = new HashMap<>();
        var popularProducts = productRepository.getProductByStatus("popular");

        try {
            for (Product product : popularProducts) {
                popularProductsMap.put(String.valueOf(product.id()), objectMapper.writeValueAsString(product));
            }

            if (!popularProductsMap.isEmpty()) {
                redisClient.hset(POPULAR_HASH_KEY, popularProductsMap);
                redisClient.expire(POPULAR_HASH_KEY, 3600);
            }

            return popularProducts;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Product> getProductByStatus(String status) {
        if (status.equalsIgnoreCase("popular")) {
            return getPopularProducts();
        }

        return productRepository.getProductByStatus(status);
    }

    public Product getProductById(String id) {
        return productRepository.getProductById(Long.parseLong(id));
    }

    public Product createAProduct(ProductDTO productDTO) {
        var createdProduct = productRepository.createNewProduct(productDTO);

        if (createdProduct != null && redisClient.exists(POPULAR_HASH_KEY)) {
            if ("popular".equalsIgnoreCase(createdProduct.status())) {
                try {
                    redisClient.hset(POPULAR_HASH_KEY, String.valueOf(createdProduct.id()), objectMapper.writeValueAsString(createdProduct));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }

        return createdProduct;
    }

    public Product updateAProduct(Product product) {
        boolean updated = productRepository.updateAProduct(product);

        if (!updated) {
            return null;
        }

        if (redisClient.exists(POPULAR_HASH_KEY)) {
            if ("popular".equalsIgnoreCase(product.status())) {
                try {
                    redisClient.hset(POPULAR_HASH_KEY, String.valueOf(product.id()), objectMapper.writeValueAsString(product));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                redisClient.hdel(POPULAR_HASH_KEY, String.valueOf(product.id()));
            }
        }

        return product;
    }

    public void deleteAProduct(long productId) {
        productRepository.deleteAProduct(productId);
        redisClient.hdel(POPULAR_HASH_KEY, String.valueOf(productId));
    }

    private static class Holder {
        private static final ProductService INSTANCE = new ProductService();
    }
}