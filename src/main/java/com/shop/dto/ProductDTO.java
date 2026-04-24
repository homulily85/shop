package com.shop.dto;

public record ProductDTO(String title,
                         long price,
                         String description,
                         long availableQuantity,
                         String category,
                         String status,
                         String imageLink) {
    public ProductDTO {
        if (title == null || title.isBlank() || price < 0 || availableQuantity < 0) {
            throw new IllegalArgumentException("Invalid product data");
        }
    }
}
