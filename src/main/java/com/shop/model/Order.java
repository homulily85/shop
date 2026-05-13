package com.shop.model;

import java.time.LocalDateTime;
import java.util.List;

public record Order(long id, long customerId, long totalAmount, String status, Long transactionsId,
                    List<OrderItem> items, LocalDateTime updatedAt) {
}


