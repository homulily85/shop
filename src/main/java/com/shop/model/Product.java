package com.shop.model;

public record Product(
        long id,
        String title,
        long price,
        String description,
        long availableQuantity,
        String category,
        String status,
        String imageLink
) {

}