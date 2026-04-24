package com.shop.repository;

import com.shop.database.DatabaseManager;
import com.shop.model.OrderItem;
import com.shop.model.Order;
import com.shop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private static final String ORDER_TABLE_NAME = "orders";
    private static final String ORDER_ALIAS = "o";
    private static final String ORDER_ID = "id";
    private static final String ORDER_CUSTOMER_ID = "customer_id";
    private static final String ORDER_TOTAL_AMOUNT = "total_amount";
    private static final String ORDER_STATUS = "status";
    private static final String ORDER_ID_ALIAS = "order_id";

    private static final String ITEM_TABLE_NAME = "order_items";
    private static final String ITEM_ALIAS = "i";
    private static final String ITEM_ORDER_ID = "order_id";
    private static final String ITEM_PRODUCT_ID = "product_id";
    private static final String ITEM_QUANTITY = "quantity";

    private static final String PRODUCT_TABLE_NAME = "products";
    private static final String PRODUCT_ALIAS = "p";
    private static final String PRODUCT_ID = "id";
    private static final String PRODUCT_TITLE = "title";
    private static final String PRODUCT_PRICE = "price";
    private static final String PRODUCT_DESCRIPTION = "description";
    private static final String PRODUCT_QUANTITY = "quantity";
    private static final String PRODUCT_CATEGORY = "category";
    private static final String PRODUCT_STATUS = "status";
    private static final String PRODUCT_IMAGE_LINK = "image_link";

    private OrderRepository() {
    }

    public static OrderRepository getInstance() {
        return Holder.INSTANCE;
    }

    public Order getOrderById(long id) {
        String sql = "SELECT " +
                ORDER_ALIAS + "." + ORDER_ID + " AS " + ORDER_ID_ALIAS + ", " +
                ORDER_ALIAS + "." + ORDER_CUSTOMER_ID + ", " +
                ORDER_ALIAS + "." + ORDER_TOTAL_AMOUNT + ", " +
                ORDER_ALIAS + "." + ORDER_STATUS + ", " +
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

                        order = new Order(orderId, customerId, totalAmount, status, items);
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

    private static class Holder {
        private static final OrderRepository INSTANCE = new OrderRepository();
    }
}