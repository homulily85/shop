package com.shop.model;

public class BillItem {
    private final long id;
    private final long billId;
    private final long productId;
    private final int quantity;

    public BillItem(long id, long billId, long productId, int quantity) {
        this.id = id;
        this.billId = billId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public BillItem(long billId, long productId, int quantity) {
        this.id = -1;
        this.billId = billId;
        this.productId = productId;
        this.quantity = quantity;
    }


    public long getBillId() {
        return billId;
    }

    public long getProductId() {
        return productId;
    }


    public int getQuantity() {
        return quantity;
    }


    @Override
    public String toString() {
        return "BillItem{" +
                "id=" + id +
                ", billId=" + billId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
