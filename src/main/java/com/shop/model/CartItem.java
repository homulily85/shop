package com.shop.model;

public record CartItem(
        Product product,
        long orderedQuantity
) {
}