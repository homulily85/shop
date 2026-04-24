package com.shop.model;

public record OrderItem(long orderId, Product product, long quantity) {

}
