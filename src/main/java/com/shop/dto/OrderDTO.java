package com.shop.dto;

import com.shop.model.OrderItem;

import java.util.List;

public record OrderDTO(long customerId, long totalAmount, List<OrderItem> items) {
}
