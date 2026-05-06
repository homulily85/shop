package com.shop.service;

import com.shop.dto.OrderDTO;
import com.shop.model.Order;
import com.shop.repository.OrderRepository;

public class OrderService {
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final CartService cartService = CartService.getInstance();

    private OrderService() {
    }

    /**
     * Provides access to the singleton instance of OrderService.
     *
     * @return Singleton instance of OrderService.
     */
    public static OrderService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get an order by its ID.
     *
     * @param id Order ID as a string. It will be parsed to a long before querying the database.
     * @return Order with the given ID, or null if not found.
     * @throws NumberFormatException if the provided ID string cannot be parsed to a long.
     */
    public Order getOrderById(String id) {
        return orderRepository.getOrderById(Long.parseLong(id));
    }

    /**
     * Checkout the cart for the given cart ID and customer ID.
     *
     * @param customerId ID of the customer placing the order.
     * @throws IllegalStateException if the cart is empty.
     */
    public long checkout(String customerId) {
        var cart = cartService.getCart(customerId);
        if (cart == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty!");
        }

        long totalAmount =
                cart.items().stream().mapToLong(item -> item.product().price() * item.orderedQuantity())
                        .sum();

        return orderRepository.createPendingOrderTransaction(new OrderDTO(Long.parseLong(customerId), totalAmount, cart.items()));


    }

    private static class Holder {
        private static final OrderService INSTANCE = new OrderService();
    }
}
