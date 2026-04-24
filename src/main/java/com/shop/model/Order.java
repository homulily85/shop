package com.shop.model;

public record Order(long id, long customerId, long totalAmount, String status) {
}


