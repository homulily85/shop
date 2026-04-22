package com.shop.model;

import com.shop.utility.MinimalJsonParser;

import java.util.Map;

public class Product {
    private final long id;
    private final String title;
    private final long price;
    private final String description;
    private final long quantity;
    private final String category;
    private final String status;
    private final String imageLink;

    public Product(long id, String title, long price, String description, long quantity,
                   String category, String status, String imageLink) {
        validate(title, price, quantity);
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
        this.category = category;
        this.status = status;
        this.imageLink = imageLink;
    }

    public Product(String title, long price, String description, long quantity, String category,
                   String status, String imageLink) {
        validate(title, price, quantity);
        this.id = -1;
        this.title = title;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
        this.category = category;
        this.status = status;
        this.imageLink = imageLink;
    }

    public static Product parseProduct(String json) {
        try {
            MinimalJsonParser parser = new MinimalJsonParser(json);

            @SuppressWarnings("unchecked")
            Map<String, Object> jsonBody = (Map<String, Object>) parser.parse();

            String title = (String) jsonBody.get("title");

            long price = ((Number) jsonBody.get("price")).longValue();
            long quantity = ((Number) jsonBody.get("quantity")).longValue();

            String description = (String) jsonBody.get("description");
            String category = (String) jsonBody.get("category");
            String imageLink = (String) jsonBody.get("image_link");
            String status = (String) jsonBody.get("status");

            return new Product(title, price, description, quantity, category, status, imageLink);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid product data");
        }
    }

    private static void validate(String title, long price, long quantity) {
        if (title.isBlank() || price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid product data");
        }
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public long getQuantity() {
        return quantity;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public String getImageLink() {
        return imageLink;
    }

    @Override
    public String toString() {
        return """
                {
                "id": %d,
                "title": "%s",
                "price": %d,
                "description": "%s",
                "quantity": %d,
                "category": "%s",
                "status": "%s",
                "imageLink": "%s"
                }
                """.formatted(id, title, price, description, quantity, category, status, imageLink);
    }
}
