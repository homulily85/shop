package com.shop.service;

import com.shop.client.payment.PaymentApiClient;
import com.shop.dto.OrderDTO;
import com.shop.model.Order;
import com.shop.repository.OrderRepository;

public class OrderService {
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final CartService cartService = CartService.getInstance();
    private final PaymentApiClient paymentApiClient = PaymentApiClient.getInstance();

    private OrderService() {
    }

    public static OrderService getInstance() {
        return Holder.INSTANCE;
    }

    public Order getOrderById(String id) {
        return orderRepository.getOrderById(Long.parseLong(id));
    }

    public long checkout(String customerId) {
        var cart = cartService.getCart(customerId);
        if (cart == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty!");
        }

        long totalAmount =
                cart.items().stream().mapToLong(item -> item.product().price() * item.orderedQuantity())
                        .sum();

        var newOderId =
                orderRepository.createPendingOrderTransaction(new OrderDTO(Long.parseLong(customerId), totalAmount, cart.items()));

        long returnedTransactionId;
        try {
            returnedTransactionId = paymentApiClient.makePayment(newOderId,
                    Long.parseLong(customerId),
                    totalAmount);
        } catch (IllegalArgumentException e) {
            if ("INSUFFICIENT_BALANCE".equals(e.getMessage())) {
                orderRepository.markOrderFailedAndRestoreStock(newOderId, cart.items());
                throw new IllegalArgumentException("Payment failed: Insufficient balance.");
            }
            throw e;
        }

        if (returnedTransactionId < 0) {
            orderRepository.markOrderFailedAndRestoreStock(newOderId, cart.items());
            throw new RuntimeException("Payment service unavailable. Your order was cancelled and" +
                    " you were not charged.");
        }

        orderRepository.updateTransactionId(newOderId, returnedTransactionId);

        return returnedTransactionId;
    }

    public void handleCallback(String orderId, boolean success) {
        long orderIdLong = Long.parseLong(orderId);
        var order = orderRepository.getOrderById(orderIdLong);
        if (order == null) {
            throw new IllegalArgumentException("Order not found for ID: " + orderId);
        }

        if (success) {
            orderRepository.updateOrderStatus(orderIdLong, "SUCCESS");
            cartService.clearCart(String.valueOf(order.customerId()));
        } else {
            orderRepository.markOrderFailedAndRestoreStock(orderIdLong, order.items());
        }
    }

    private static class Holder {
        private static final OrderService INSTANCE = new OrderService();
    }
}