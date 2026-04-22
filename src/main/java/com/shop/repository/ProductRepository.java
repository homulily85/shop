package com.shop.repository;

import com.shop.database.DatabaseManager;
import com.shop.dto.ProductDTO;
import com.shop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private static final String TABLE_NAME = "products";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String PRICE = "price";
    private static final String DESCRIPTION = "description";
    private static final String QUANTITY = "quantity";
    private static final String CATEGORY = "category";
    private static final String STATUS = "status";
    private static final String IMAGE_LINK = "image_link";
    private static final int ID_INDEX = 1;
    private static final int TITLE_INDEX = 2;
    private static final int PRICE_INDEX = 3;
    private static final int DESCRIPTION_INDEX = 4;
    private static final int QUANTITY_INDEX = 5;
    private static final int CATEGORY_INDEX = 6;
    private static final int STATUS_INDEX = 7;
    private static final int IMAGE_LINK_INDEX = 8;

    private ProductRepository() {
    }

    public static ProductRepository getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get paginated products from the database.
     *
     * @return List of products.
     */
    public List<Product> getAllProducts(int pageNumber, int pageSize) {
        String sql = "SELECT * FROM %s LIMIT ? OFFSET ?".formatted(TABLE_NAME);
        return executeProductQuery(sql, pageSize, pageNumber * pageSize);
    }

    /**
     * Get a product by its ID.
     *
     * @param id Product ID.
     * @return Product with the given ID, or null if not found.
     */
    public Product getProductById(long id) {
        String sql = "SELECT * FROM %s WHERE %s = ?".formatted(TABLE_NAME, ID);
        var products = executeProductQuery(sql, id);
        return products.isEmpty() ? null : products.getFirst();
    }

    /**
     * Get products by category.
     *
     * @param status Product status.
     * @return List of products with the given status.
     */
    public List<Product> getProductByStatus(String status) {
        String sql = "SELECT * FROM %s WHERE %s = ?".formatted(TABLE_NAME, STATUS);
        return executeProductQuery(sql, status);
    }

    /**
     * Create a new product in the database.
     *
     * @param productDTO Product to be created.
     * @return The created product with the generated ID, or null if creation failed.
     */
    public Product createNewProduct(ProductDTO productDTO) {
        String sql =
                "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)".formatted(TABLE_NAME, TITLE, PRICE, DESCRIPTION, QUANTITY, CATEGORY, STATUS, IMAGE_LINK);
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement query =
                connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            query.setObject(1, productDTO.title());
            query.setObject(2, productDTO.price());
            query.setObject(3, productDTO.description());
            query.setObject(4, productDTO.quantity());
            query.setObject(5, productDTO.category());
            query.setObject(6, productDTO.status());
            query.setObject(7, productDTO.imageLink());

            query.executeUpdate();
            try (ResultSet generatedKeys = query.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(ID_INDEX);
                    return new Product(id, productDTO.title(), productDTO.price(),
                            productDTO.description(), productDTO.quantity(), productDTO.category(),
                            productDTO.status(), productDTO.imageLink());
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing product in the database.
     *
     * @param product Product to be updated.
     * @return True if the product was updated successfully, false otherwise.
     */
    public boolean updateAProduct(Product product) {
        String sql = ("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ? " +
                "WHERE" + " %s = ?").formatted(TABLE_NAME, TITLE, PRICE, DESCRIPTION, QUANTITY,
                CATEGORY, STATUS, IMAGE_LINK, ID);
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement query =
                connection.prepareStatement(sql)) {
            query.setObject(1, product.title());
            query.setObject(2, product.price());
            query.setObject(3, product.description());
            query.setObject(4, product.quantity());
            query.setObject(5, product.category());
            query.setObject(6, product.status());
            query.setObject(7, product.id());
            query.setObject(8, product.id());

            return query.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a product from the database.
     *
     * @param productId ID of the product to be deleted.
     */
    public void deleteAProduct(Long productId) {
        String sql = "DELETE FROM %s WHERE %s = ?".formatted(TABLE_NAME, ID);
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement query =
                connection.prepareStatement(sql)) {
            query.setObject(1, productId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to execute a query with secure parameters and map the result.
     */
    private List<Product> executeProductQuery(String sql, Object... params) {
        List<Product> result = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement query =
                connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                query.setObject(i + 1, params[i]);
            }

            try (ResultSet resultSet = query.executeQuery()) {
                while (resultSet.next()) {
                    var product = new Product(resultSet.getLong(ID_INDEX),
                            resultSet.getString(TITLE_INDEX), resultSet.getLong(PRICE_INDEX),
                            resultSet.getString(DESCRIPTION_INDEX),
                            resultSet.getInt(QUANTITY_INDEX), resultSet.getString(CATEGORY_INDEX)
                            , resultSet.getString(STATUS_INDEX),
                            resultSet.getString(IMAGE_LINK_INDEX));
                    result.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return result;
    }

    // "Bill Pugh" Singleton to ensure thread safe.
    private static class Holder {
        private static final ProductRepository INSTANCE = new ProductRepository();
    }
}
