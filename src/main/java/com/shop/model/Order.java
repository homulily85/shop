package com.shop.model;

import java.util.List;

public record Order(long id, long customerId, long totalAmount, List<OrderItem> items) {
}


