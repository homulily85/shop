package com.shop.model;

import java.util.List;

public record Order(long id, long customerId, long totalAmount, String status,
                    List<OrderItem> items) {
}


