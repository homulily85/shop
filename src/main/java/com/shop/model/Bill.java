package com.shop.model;

public class Bill {
    private final long id;
    private final long customerId;
    private final long totalAmount;

    public Bill(long id, long customerId, long totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }

    public Bill(long totalAmount, long customerId) {
        this.id = -1;
        this.totalAmount = totalAmount;
        this.customerId = customerId;
    }

    public long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", totalAmount=" + totalAmount +
                '}';
    }
}


