package com.shop.repository;

import com.shop.database.DatabaseManager;
import com.shop.model.Cart;
import com.shop.model.CartItem;
import com.shop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    private static final String CART_TABLE_NAME = "carts";
    private static final String CART_ALIAS = "c";
    private static final String CART_ID = "id";
    private static final String CART_CUSTOMER_ID = "customer_id";
    private static final String CART_STATUS = "status";
    private static final String CART_ID_ALIAS = "cart_id";

    private static final String ITEM_TABLE_NAME = "cart_items";
    private static final String ITEM_ALIAS = "i";
    private static final String ITEM_CART_ID = "cart_id";
    private static final String ITEM_PRODUCT_ID = "product_id";
    private static final String ITEM_QUANTITY = "quantity";

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

    private CartRepository() {

    }

    public static CartRepository getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get the items in the active cart for a given customer ID.
     *
     * @param customerId ID of the customer whose active cart items are to be retrieved.
     * @return Cart object containing the active cart and its items for the specified customer.
     * If no active cart exists, returns null.
     */
    public Cart getCartItemsInActiveCartOfCustomer(long customerId) {
        String sql = "SELECT " +
                CART_ALIAS + "." + CART_ID + " AS " + CART_ID_ALIAS + ", " +
                CART_ALIAS + "." + CART_CUSTOMER_ID + ", " +
                ITEM_ALIAS + "." + ITEM_PRODUCT_ID + ", " +
                ITEM_ALIAS + "." + ITEM_QUANTITY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_TITLE + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_PRICE + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_DESCRIPTION + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_QUANTITY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_CATEGORY + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_STATUS + ", " +
                PRODUCT_ALIAS + "." + PRODUCT_IMAGE_LINK + " " +
                "FROM " + CART_TABLE_NAME + " " + CART_ALIAS + " " +
                "LEFT JOIN " + ITEM_TABLE_NAME + " " + ITEM_ALIAS +
                " ON " + CART_ALIAS + "." + CART_ID + " = " + ITEM_ALIAS + "." + ITEM_CART_ID +
                " " +
                " LEFT JOIN " + PRODUCT_TABLE_NAME + " " + PRODUCT_ALIAS + " " +
                " ON " + PRODUCT_ALIAS + "." + PRODUCT_ID + " = " + ITEM_ALIAS + "." + ITEM_PRODUCT_ID +
                " WHERE " + CART_ALIAS + "." + CART_CUSTOMER_ID + " = ?" +
                " AND " + CART_ALIAS + "." + CART_STATUS + " = 'ACTIVE'";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                Cart cart = null;
                List<CartItem> items = new ArrayList<>();

                while (rs.next()) {
                    if (cart == null) {
                        long cartId = rs.getLong(CART_ID_ALIAS);
                        long returnedCustomerId = rs.getLong(CART_CUSTOMER_ID);
                        String status = rs.getString(CART_STATUS);

                        cart = new Cart(cartId, returnedCustomerId, status, items);
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
                    items.add(new CartItem(new Product(productId, productTitle, productPrice,
                            productDescription, productQuantity,
                            productCategory, productStatus, productImageLink)
                            , quantity));
                }
                return cart;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the ID of the active cart for a given customer ID.
     *
     * @param customerId ID of the customer whose active cart ID is to be retrieved.
     * @return ID of the active cart for the specified customer. If no active cart exists,
     * returns -1.
     */
    public long getActiveCartOfCustomer(long customerId) {
        String sql = "SELECT " + CART_ID + " FROM " + CART_TABLE_NAME +
                " WHERE " + CART_CUSTOMER_ID + " = ?" +
                " AND " + CART_STATUS + " = 'ACTIVE'";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(CART_ID);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Create a new active cart for a given customer ID.
     *
     * @param customerId ID of the customer for whom the new active cart is to be created.
     * @return ID of the newly created active cart for the specified customer. If creation fails,
     * returns -1.
     */
    public long createNewActiveCartForCustomer(long customerId) {
        String sql =
                "INSERT INTO " + CART_TABLE_NAME + " (" + CART_CUSTOMER_ID + ", " + CART_STATUS + ") VALUES (?, 'ACTIVE')";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, customerId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating cart failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating cart failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long getQuantityOfAItem(long cartId, long productId) {
        String sql = "SELECT " + ITEM_QUANTITY + " FROM " + ITEM_TABLE_NAME +
                " WHERE " + ITEM_CART_ID + " = ?" +
                " AND " + ITEM_PRODUCT_ID + " = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, cartId);
            stmt.setLong(2, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(ITEM_QUANTITY);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Add an item to the active cart for a given customer ID.
     *
     * @param activeCartId ID of the active cart to which the item is to be added.
     * @param productId    ID of the product to be added to the cart.
     * @param quantity     Quantity of the product to be added to the cart.
     */
    public void addItemToCart(long activeCartId, long productId, long quantity) {
        String sql = "INSERT INTO " + ITEM_TABLE_NAME + " (" + ITEM_CART_ID + ", " +
                ITEM_PRODUCT_ID + ", " + ITEM_QUANTITY + ") VALUES (?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, activeCartId);
            stmt.setLong(2, productId);
            stmt.setLong(3, quantity);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the quantity of an item in a cart
     *
     * @param cartId    ID of the cart in which the item is to be updated
     * @param productId ID of the product whose quantity is to be updated
     * @param quantity  New quantity of the product in the cart
     */
    public void updateCartItem(long cartId, long productId, long quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        String sql = "UPDATE " + ITEM_TABLE_NAME + " SET " + ITEM_QUANTITY + " = ? " +
                "WHERE " + ITEM_CART_ID + " = ? AND " + ITEM_PRODUCT_ID + " = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, quantity);
            stmt.setLong(2, cartId);
            stmt.setLong(3, productId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove an item from the cart
     *
     * @param cartId    ID of the cart from which the item is to be removed
     * @param productId ID of the product to be removed from the cart
     */
    public void removeItemFromCart(long cartId, long productId) {
        String sql = "DELETE FROM " + ITEM_TABLE_NAME +
                " WHERE " + ITEM_CART_ID + " = ? AND " + ITEM_PRODUCT_ID + " = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, cartId);
            stmt.setLong(2, productId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make a cart inactive
     *
     * @param cartId ID of the cart to be made inactive
     */
    public void makeACartInactive(long cartId) {
        String sql = "UPDATE " + CART_TABLE_NAME + " SET " + CART_STATUS + " = 'INACTIVE' " +
                "WHERE " + CART_ID + " = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, cartId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static class Holder {
        private static final CartRepository INSTANCE = new CartRepository();
    }
}
