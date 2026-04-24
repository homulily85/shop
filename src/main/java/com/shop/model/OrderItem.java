package com.shop.model;

public record OrderItem(
        Product product,
        long orderedQuantity
) {
}