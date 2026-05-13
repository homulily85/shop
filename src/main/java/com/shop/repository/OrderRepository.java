package com.shop.repository;

import com.shop.database.DatabaseManager;
import com.shop.dto.OrderDTO;
import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {
    private static final String ORDER_TABLE_NAME = "orders";
    private static final String ORDER_ALIAS = "o";
    private static final String ORDER_ID = "id";
    private static final String ORDER_CUSTOMER_ID = "customer_id";
    private static final String ORDER_TOTAL_AMOUNT = "total_amount";
    private static final String ORDER_STATUS = "status";
    private static final String ORDER_TRANSACTION_ID = "transaction_id";
    private static final String ORDER_UPDATED_AT = "updated_at";
    private static final String ORDER_ID_ALIAS = "order_id";

    private static final String ITEM_TABLE_NAME = "order_items";
    private static final String ITEM_ALIAS = "i";
    private static final String ITEM_ORDER_ID = "order_id";
    private static final String ITEM_PRODUCT_ID = "product_id";
    private static final String ITEM_QUANTITY = "ordered_quantity";

    private static final String PRODUCT_TABLE_NAME = "products";
    private static final String PRODUCT_ALIAS = "p";
    private static final String PRODUCT_ID = "id";
    private static final String PRODUCT_TITLE = "title";
    private static final String PRODUCT_PRICE = "price";
    private static final String PRODUCT_DESCRIPTION = "description";
    private static final String PRODUCT_QUANTITY = "available_quantity";
    private static final String PRODUCT_CATEGORY = "category";
    private static final String PRODUCT_STATUS = "status";
    private static final String PRODUCT_IMAGE_LINK = "image_link";

    private OrderRepository() {
    }

    /**
     * Provide access to the singleton instance of OrderRepository.
     *
     * @return Singleton instance of OrderRepository.
     */
    public static OrderRepository getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get an order by its ID.
     *
     * @param id Order ID.
     * @return Order with the given ID, or null if not found.
     */
    public Order getOrderById(long id) {
        String sql = "SELECT " +
                ORDER_ALIAS + "." + ORDER_ID + " AS " + ORDER_ID_ALIAS + ", " +
                ORDER_ALIAS + "." + ORDER_CUSTOMER_ID + ", " +
                ORDER_ALIAS + "." + ORDER_TOTAL_AMOUNT + ", " +
                ORDER_ALIAS + "." + ORDER_STATUS + ", " +
                ORDER_ALIAS + "." + ORDER_TRANSACTION_ID + ", " +
                ORDER_ALIAS + "." + ORDER_UPDATED_AT + ", " +
                ITEM_ALIAS + "." + ITEM_PRODUCT_ID + ", " +
                ITEM_ALIAS + "." + ITEM_QUANTITY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_TITLE + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_PRICE + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_DESCRIPTION + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_QUANTITY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_CATEGORY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_STATUS + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_IMAGE_LINK + " " +
                "FROM " + ORDER_TABLE_NAME + " " + ORDER_ALIAS + " " +
                "LEFT JOIN " + ITEM_TABLE_NAME + " " + ITEM_ALIAS +
                " ON " + ORDER_ALIAS + "." + ORDER_ID + " = " + ITEM_ALIAS + "." + ITEM_ORDER_ID + " " +
                " LEFT JOIN " + PRODUCT_TABLE_NAME + " " + PRODUCT_ALIAS + " " +
                " ON " + PRODUCT_ALIAS + "." + PRODUCT_ID + " = " + ITEM_ALIAS + "." + ITEM_PRODUCT_ID +
                " WHERE " + ORDER_ALIAS + "." + ORDER_ID + " = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                Order order = null;
                List<OrderItem> items = new ArrayList<>();

                while (rs.next()) {
                    if (order == null) {
                        long orderId = rs.getLong(ORDER_ID_ALIAS);
                        long customerId = rs.getLong(ORDER_CUSTOMER_ID);
                        long totalAmount = rs.getLong(ORDER_TOTAL_AMOUNT);
                        String status = rs.getString(ORDER_STATUS);
                        Long billId = rs.getLong(ORDER_TRANSACTION_ID);
                        LocalDateTime updatedAt =
                                LocalDateTime.of(rs.getDate(ORDER_UPDATED_AT).toLocalDate(),
                                        rs.getTime(ORDER_UPDATED_AT).toLocalTime());

                        order = new Order(orderId, customerId, totalAmount, status, billId, items,
                                updatedAt.toString());
                    }

                    long productId = rs.getLong(ITEM_PRODUCT_ID);
                    String productTitle = rs.getString(PRODUCT_TITLE);
                    long productPrice = rs.getLong(PRODUCT_PRICE);
                    String productDescription = rs.getString(PRODUCT_DESCRIPTION);
                    long productQuantity = rs.getLong(PRODUCT_QUANTITY);
                    String productCategory = rs.getString(PRODUCT_CATEGORY);
                    String productStatus = rs.getString(PRODUCT_STATUS);
                    String productImageLink = rs.getString(PRODUCT_IMAGE_LINK);

                    int quantity = rs.getInt(ITEM_QUANTITY);
                    items.add(new OrderItem(new Product(productId, productTitle, productPrice,
                            productDescription, productQuantity,
                            productCategory, productStatus, productImageLink)
                            , quantity));
                }
                return order;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Order> getOrderByStatus(String status) {
        String sql = "SELECT " +
                ORDER_ALIAS + "." + ORDER_ID + " AS " + ORDER_ID_ALIAS + ", " +
                ORDER_ALIAS + "." + ORDER_CUSTOMER_ID + ", " +
                ORDER_ALIAS + "." + ORDER_TOTAL_AMOUNT + ", " +
                ORDER_ALIAS + "." + ORDER_STATUS + ", " +
                ORDER_ALIAS + "." + ORDER_TRANSACTION_ID + ", " +
                ORDER_ALIAS + "." + ORDER_UPDATED_AT + ", " +
                ITEM_ALIAS + "." + ITEM_PRODUCT_ID + ", " +
                ITEM_ALIAS + "." + ITEM_QUANTITY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_TITLE + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_PRICE + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_DESCRIPTION + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_QUANTITY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_CATEGORY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_STATUS + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_IMAGE_LINK + " " +
                "FROM " + ORDER_TABLE_NAME + " " + ORDER_ALIAS + " " +
                "LEFT JOIN " + ITEM_TABLE_NAME + " " + ITEM_ALIAS +
                " ON " + ORDER_ALIAS + "." + ORDER_ID + " = " + ITEM_ALIAS + "." + ITEM_ORDER_ID + " " +
                " LEFT JOIN " + PRODUCT_TABLE_NAME + " " + PRODUCT_ALIAS + " " +
                " ON " + PRODUCT_ALIAS + "." + PRODUCT_ID + " = " + ITEM_ALIAS + "." + ITEM_PRODUCT_ID +
                " WHERE " + ORDER_ALIAS + "." + ORDER_STATUS + " = ?";


        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                // Ensure orders are returned in the order they were created
                Map<Long, Order> orderMap = new LinkedHashMap<>();

                while (rs.next()) {
                    long orderId = rs.getLong(ORDER_ID_ALIAS);

                    Order order = orderMap.get(orderId);
                    if (order == null) {
                        long customerId = rs.getLong(ORDER_CUSTOMER_ID);
                        long totalAmount = rs.getLong(ORDER_TOTAL_AMOUNT);
                        String orderStatus = rs.getString(ORDER_STATUS);
                        Long billId = rs.getLong(ORDER_TRANSACTION_ID);
                        LocalDateTime updatedAt =
                                LocalDateTime.of(rs.getDate(ORDER_UPDATED_AT).toLocalDate(),
                                        rs.getTime(ORDER_UPDATED_AT).toLocalTime());


                        if (rs.wasNull()) {
                            billId = null;
                        }

                        order = new Order(orderId, customerId, totalAmount, orderStatus, billId,
                                new ArrayList<>(), updatedAt.toString());
                        orderMap.put(orderId, order);
                    }

                    long productId = rs.getLong(ITEM_PRODUCT_ID);
                    String productTitle = rs.getString(PRODUCT_TITLE);
                    long productPrice = rs.getLong(PRODUCT_PRICE);
                    String productDescription = rs.getString(PRODUCT_DESCRIPTION);
                    long productQuantity = rs.getLong(PRODUCT_QUANTITY);
                    String productCategory = rs.getString(PRODUCT_CATEGORY);
                    String productStatus = rs.getString(PRODUCT_STATUS);
                    String productImageLink = rs.getString(PRODUCT_IMAGE_LINK);

                    int quantity = rs.getInt(ITEM_QUANTITY);

                    Product product = new Product(productId, productTitle, productPrice,
                            productDescription, productQuantity,
                            productCategory, productStatus, productImageLink);

                    order.items().add(new OrderItem(product, quantity));
                }

                return new ArrayList<>(orderMap.values());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public long createPendingOrderTransaction(OrderDTO order) {
        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {
                deductProductStock(connection, order.items());
                long orderId = insertPendingOrder(connection, order);
                insertOrderItems(connection, orderId, order.items());

                connection.commit();
                return orderId;

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Checkout transaction failed: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection error", e);
        }
    }

    private void deductProductStock(Connection connection, List<OrderItem> items) throws SQLException {
        String sql =
                "UPDATE " + PRODUCT_TABLE_NAME +
                        " SET " + PRODUCT_QUANTITY + " = " + PRODUCT_QUANTITY + " - ?" +
                        " WHERE " + PRODUCT_ID + " = ? AND " + PRODUCT_QUANTITY + " >= ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (OrderItem item : items) {
                stmt.setLong(1, item.orderedQuantity());
                stmt.setLong(2, item.product().id());
                stmt.setLong(3, item.orderedQuantity());

                stmt.addBatch();
            }

            int[] affectedRowsArray = stmt.executeBatch();

            for (int i = 0; i < affectedRowsArray.length; i++) {
                if (affectedRowsArray[i] == 0) {
                    OrderItem failedItem = items.get(i);
                    throw new RuntimeException("Insufficient stock for product ID: " + failedItem.product().id());
                }
            }
        }
    }

    private long insertPendingOrder(Connection connection, OrderDTO order) throws SQLException {
        String sql = "INSERT INTO " + ORDER_TABLE_NAME +
                " ( " + ORDER_CUSTOMER_ID + ", " + ORDER_TOTAL_AMOUNT + ", " + ORDER_STATUS + " )" +
                " VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, order.customerId());
            stmt.setLong(2, order.totalAmount());
            stmt.setString(3, "PENDING");
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Failed to retrieve generated order ID.");
                }
            }
        }
    }

    private void insertOrderItems(Connection connection, long orderId, List<OrderItem> items) throws SQLException {
        String sql =
                "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)".formatted(ITEM_TABLE_NAME,
                        ORDER_ID_ALIAS, ITEM_PRODUCT_ID, ITEM_QUANTITY);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (OrderItem item : items) {
                stmt.setLong(1, orderId);
                stmt.setLong(2, item.product().id());
                stmt.setLong(3, item.orderedQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void updateTransactionId(long orderId, long transactionId) {
        String sql = "UPDATE %s SET %s = ? WHERE %s = ?".formatted(
                ORDER_TABLE_NAME, ORDER_TRANSACTION_ID, ORDER_ID);

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, transactionId);
            stmt.setLong(2, orderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void updateOrderStatus(long orderId, String status) {
        String sql = "UPDATE %s SET %s = ? WHERE %s = ?".formatted(
                ORDER_TABLE_NAME, ORDER_STATUS, ORDER_ID);

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setLong(2, orderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Compensating transaction to run if the 3rd-party payment initiation fails.
     * Marks the order as FAILED and restores product inventory.
     */
    public void markOrderFailedAndRestoreStock(long orderId, List<OrderItem> items) {
        String sqlUpdateOrder = "UPDATE %s SET %s = ? WHERE %s = ?".formatted(
                ORDER_TABLE_NAME, ORDER_STATUS, ORDER_ID);

        String sqlRestoreStock = "UPDATE %s SET %s = %s + ? WHERE %s = ?".formatted(
                PRODUCT_TABLE_NAME, PRODUCT_QUANTITY, PRODUCT_QUANTITY, PRODUCT_ID);

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtUpdateOrder = connection.prepareStatement(sqlUpdateOrder);
                 PreparedStatement stmtRestoreStock =
                         connection.prepareStatement(sqlRestoreStock)) {

                stmtUpdateOrder.setString(1, "FAILED");
                stmtUpdateOrder.setLong(2, orderId);
                stmtUpdateOrder.executeUpdate();

                for (OrderItem item : items) {
                    stmtRestoreStock.setLong(1, item.orderedQuantity());
                    stmtRestoreStock.setLong(2, item.product().id());
                    stmtRestoreStock.addBatch();
                }
                stmtRestoreStock.executeBatch();

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("CRITICAL: Failed to execute compensating transaction " +
                        "for order " + orderId, e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class Holder {
        private static final OrderRepository INSTANCE = new OrderRepository();
    }
}