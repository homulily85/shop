package com.shop.service;

import com.shop.model.Order;
import com.shop.repository.OrderRepository;

public class OrderService {
    private final OrderRepository orderRepository = OrderRepository.getInstance();

    private OrderService() {
    }

    public static OrderService getInstance() {
        return Holder.INSTANCE;
    }

    public Order getOrderById(String id) {
        return orderRepository.getOrderById(Long.parseLong(id));
    }

    private static class Holder {
        private static final OrderService INSTANCE = new OrderService();
    }
}
