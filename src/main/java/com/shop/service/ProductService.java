package com.shop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.ProductDTO;
import com.shop.model.Product;
import com.shop.redis.Client;
import com.shop.repository.ProductRepository;
import redis.clients.jedis.RedisClient;

import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Provides access to the singleton instance of ProductService.
     *
     * @return Singleton instance of ProductService.
     */
    public static ProductService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get the total number of products.
     *
     * @return Total product count.
     */
    public long getTotalProductCount() {
        return productRepository.getTotalProductCount();
    }

    /**
     * Get paginated and sorted products from the database.
     *
     * @param pageNumber Page index to retrieve.
     * @param pageSize   Number of items per page.
     * @param sortBy     Field to sort by.
     * @param sortOrder  Direction of sort ("asc" or "desc").
     * @return List of products.
     */
    public List<Product> getAllProducts(int pageNumber, int pageSize, String sortBy,
                                        String sortOrder) {
        return productRepository.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
    }

    /**
     * Get the total number of products by status.
     *
     * @param status Product status to filter by.
     * @return Total product count by status.
     */
    public long getProductCountByStatus(String status) {
        if (status.equalsIgnoreCase("popular")) {
            return getPopularProducts().size();
        }

        return productRepository.getProductCountByStatus(status);
    }

    /**
     * Get popular products.
     *
     * @return List of popular products.
     * @throws RuntimeException if there is an error processing JSON data for caching popular
     *                          products in Redis.
     */
    private List<Product> getPopularProducts() {
        // Check if popular products are cached in Redis
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
                throw new RuntimeException(e);
            }
        }

        // If not cached, fetch from the database and cache the results in Redis
        Map<String, String> popularProductsMap = new HashMap<>();
        var popularProducts = productRepository.getProductByStatus("popular");

        try {
            for (Product product : popularProducts) {
                popularProductsMap.put(String.valueOf(product.id()),
                        objectMapper.writeValueAsString(product));
            }

            if (!popularProductsMap.isEmpty()) {
                redisClient.hset(POPULAR_HASH_KEY, popularProductsMap);
                redisClient.expire(POPULAR_HASH_KEY, 300);
            }

            return popularProducts;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Get products by status. If the status is "popular", it will first check the Redis cache
     * before querying the database.
     *
     * @param status Product status to filter by.
     * @return List of products with the given status.
     */
    public List<Product> getProductByStatus(String status) {
        if (status.equalsIgnoreCase("popular")) {
            return getPopularProducts();
        }

        return productRepository.getProductByStatus(status);
    }

    /**
     * Get products by status with pagination and sorting. If status is "popular", it will
     * use the cached list and apply sorting/pagination in memory.
     *
     * @param status Product status to filter by.
     * @param pageNumber Page index to retrieve.
     * @param pageSize Number of items per page.
     * @param sortBy Field to sort by.
     * @param sortOrder Direction of sort ("asc" or "desc").
     * @return List of products with the given status.
     */
    public List<Product> getProductByStatus(String status, int pageNumber, int pageSize,
                                            String sortBy, String sortOrder) {
        if (status.equalsIgnoreCase("popular")) {
            var sortedProducts = sortProducts(getPopularProducts(), sortBy, sortOrder);
            return paginateProducts(sortedProducts, pageNumber, pageSize);
        }

        return productRepository.getProductByStatus(status, pageNumber, pageSize, sortBy,
                sortOrder);
    }

    /**
     * Get products by status sorted without pagination. If status is "popular", it will
     * use the cached list and apply sorting in memory.
     *
     * @param status Product status to filter by.
     * @param sortBy Field to sort by.
     * @param sortOrder Direction of sort ("asc" or "desc").
     * @return List of products with the given status.
     */
    public List<Product> getProductByStatusSorted(String status, String sortBy, String sortOrder) {
        if (status.equalsIgnoreCase("popular")) {
            return sortProducts(getPopularProducts(), sortBy, sortOrder);
        }

        return productRepository.getProductByStatusSorted(status, sortBy, sortOrder);
    }

    /**
     * Get a product by its ID.
     *
     * @param id Product ID as a string. It will be parsed to a long before querying the database.
     * @return Product with the given ID, or null if not found.
     * @throws RuntimeException if there is an error processing JSON data for caching popular
     *                          products in Redis.
     */
    public Product getProductById(String id) {
        return productRepository.getProductById(Long.parseLong(id));
    }

    /**
     * Create a new product in the database. If the product is created with "popular" status, it
     * will be cached in Redis.
     *
     * @param productDTO Product data transfer object containing the details of the product to be
     *                   created.
     * @return The created product with the generated ID, or null if creation failed.
     * @throws RuntimeException if there is an error processing JSON data for caching popular
     *                          products in Redis.
     */
    public Product createAProduct(ProductDTO productDTO) {
        var createdProduct = productRepository.createNewProduct(productDTO);

        if (createdProduct != null) {
            redisClient.del(POPULAR_HASH_KEY);
        }

        return createdProduct;
    }

    /**
     * Update an existing product in the database. If the product's status is updated to
     * "popular", it will be added to the Redis cache.
     * If the product's status is updated from "popular" to something else, it will be removed
     * from the Redis cache.
     *
     * @param product Product object containing the updated details of the product. The product
     *                must have a valid ID.
     * @return The updated product, or null if the update failed.
     * @throws RuntimeException if there is an error processing JSON data for caching popular
     *                          products in Redis.
     */
    public Product updateAProduct(Product product) {
        boolean updated = productRepository.updateAProduct(product);
        if (!updated) {
            return null;
        }

        redisClient.del(POPULAR_HASH_KEY);

        return product;
    }

    /**
     * Delete a product from the database.
     *
     * @param productId ID of the product to be deleted.
     */
    public void deleteAProduct(long productId) {
        productRepository.deleteAProduct(productId);

        redisClient.del(POPULAR_HASH_KEY);
    }

    private List<Product> sortProducts(List<Product> products, String sortBy, String sortOrder) {
        Comparator<String> nullableStringComparator = Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);
        Comparator<Product> comparator = switch (sortBy != null ? sortBy : "") {
            case "title" -> Comparator.comparing(Product::title, nullableStringComparator);
            case "price" -> Comparator.comparingLong(Product::price);
            case "availableQuantity" -> Comparator.comparingLong(Product::availableQuantity);
            case "category" -> Comparator.comparing(Product::category, nullableStringComparator);
            case "status" -> Comparator.comparing(Product::status, nullableStringComparator);
            default -> Comparator.comparingLong(Product::id);
        };

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return products.stream().sorted(comparator).toList();
    }

    private List<Product> paginateProducts(List<Product> products, int pageNumber, int pageSize) {
        int startIndex = Math.min(pageNumber * pageSize, products.size());
        int endIndex = Math.min(startIndex + pageSize, products.size());
        return new ArrayList<>(products.subList(startIndex, endIndex));
    }

    private static class Holder {
        private static final ProductService INSTANCE = new ProductService();
    }
}