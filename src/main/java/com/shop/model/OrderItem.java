package com.shop.model;

public record OrderItem(long id, long orderId, long productId, int quantity) {

}
