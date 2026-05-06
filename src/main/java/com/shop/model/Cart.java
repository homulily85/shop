package com.shop.model;

import java.util.List;

public record Cart(long id, long customer_id, String status, List<OrderItem> items) {
}
