package com.shop.model;

public record Product(
        long id,
        String title,
        long price,
        String description,
        long quantity,
        String category,
        String status,
        String imageLink
) {

}