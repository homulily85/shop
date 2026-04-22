package com.shop.service;

import com.shop.model.Product;
import com.shop.redis.Client;
import com.shop.repository.ProductRepository;
import redis.clients.jedis.RedisClient;

import java.util.List;

public class ProductService {
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

    public String getProductByStatus(String status) {
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
        return product.toString();
    }

    public String deleteAProduct(long productId) {
        productRepository.deleteAProduct(productId);
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