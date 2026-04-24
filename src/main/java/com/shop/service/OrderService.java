package com.shop.service;

import com.shop.dto.OrderDTO;
import com.shop.model.Order;
import com.shop.repository.OrderRepository;

public class OrderService {
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final CartService cartService = CartService.getInstance();

    private OrderService() {
    }

    public static OrderService getInstance() {
        return Holder.INSTANCE;
    }

    public Order getOrderById(String id) {
        return orderRepository.getOrderById(Long.parseLong(id));
    }

    public void checkout(String cartId, long customerId) {
        var cart = cartService.getCartItems(cartId);
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        long totalAmount =
                cart.stream().mapToLong(item -> item.product().price() * item.orderedQuantity())
                        .sum();

        var orderDto = new OrderDTO(customerId, totalAmount, cart);
        orderRepository.executeCheckoutTransaction(orderDto, cart);

        cartService.clearCart(cartId);
    }

    private static class Holder {
        private static final OrderService INSTANCE = new OrderService();
    }
}
