package com.shop.service;

import com.shop.client.payment.PaymentApiClient;
import com.shop.dto.OrderDTO;
import com.shop.model.Order;
import com.shop.repository.OrderRepository;

import java.time.LocalDateTime;

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

        var newOderId = orderRepository
                .createPendingOrderTransaction(new OrderDTO(Long.parseLong(customerId),
                        totalAmount, cart.items()));

        long returnedTransactionId = -1;
        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                returnedTransactionId = paymentApiClient.makePayment(newOderId,
                        Long.parseLong(customerId),
                        totalAmount);

                if (returnedTransactionId >= 0) {
                    break;
                }
            } catch (IllegalArgumentException e) {
                if ("INSUFFICIENT_BALANCE".equals(e.getMessage())) {
                    orderRepository.markOrderFailedAndRestoreStock(newOderId, cart.items());
                    throw new IllegalArgumentException("Payment failed: Insufficient balance.");
                } else if ("ACCOUNT_NOT_FOUND".equals(e.getMessage())) {
                    orderRepository.markOrderFailedAndRestoreStock(newOderId, cart.items());
                    throw new IllegalArgumentException("Payment failed: Account not found.");
                } else {
                    throw e;
                }
            }

            attempts++;
            if (attempts < maxRetries) {
                System.out.println("[OrderService] Payment gateway unavailable. Retrying... Attempt " + attempts + " of " + maxRetries);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (returnedTransactionId < 0) {
            orderRepository.markOrderFailedAndRestoreStock(newOderId, cart.items());
            throw new RuntimeException("Payment service unavailable. Your order was cancelled and" +
                    " you were not charged.");
        }

        orderRepository.updateTransactionId(newOderId, returnedTransactionId);

        return newOderId;
    }

    public void handleCallback(String orderId, boolean success) {
        long orderIdLong = Long.parseLong(orderId);
        var order = orderRepository.getOrderById(orderIdLong);
        if (order == null) {
            throw new IllegalArgumentException("Order not found for ID: " + orderId);
        }

        if (!order.status().equals("PENDING")) {
            return;
        }

        if (success) {
            orderRepository.updateOrderStatus(orderIdLong, "SUCCESS");
            cartService.clearCart(String.valueOf(order.customerId()));
        } else {
            orderRepository.markOrderFailedAndRestoreStock(orderIdLong, order.items());
        }
    }

    public void cleanPendingOrders() {
        var pendingOrders = orderRepository.getOrderByStatus("PENDING");
        for (var order : pendingOrders) {
            if (LocalDateTime.parse(order.updatedAt()).isBefore(LocalDateTime.now().minusMinutes(15))) {
                System.out.println("[OrderService] Cleaning up pending order ID: " + order.id());
                orderRepository.markOrderFailedAndRestoreStock(order.id(), order.items());
            }
        }
    }

    private static class Holder {
        private static final OrderService INSTANCE = new OrderService();
    }
}