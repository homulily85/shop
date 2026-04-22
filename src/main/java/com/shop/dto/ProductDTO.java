package com.shop.dto;

public record ProductDTO(String title,
                         long price,
                         String description,
                         long quantity,
                         String category,
                         String status,
                         String imageLink) {
    public ProductDTO {
        if (title == null || title.isBlank() || price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid product data");
        }
    }
}
