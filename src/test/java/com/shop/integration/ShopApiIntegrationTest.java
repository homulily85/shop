package com.shop.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopApiIntegrationTest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static long createdProductId;

    @Test
    @Order(1)
    public void testHelloWorldEndpoint() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/test")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());
            JsonNode body = mapper.readTree(response.body().string());
            assertEquals("Hello World!", body.get("message").asText());
        }
    }

    @Test
    @Order(2)
    public void testCreateProduct() throws IOException {
        ObjectNode productDto = mapper.createObjectNode();
        productDto.put("title", "Test Product");
        productDto.put("price", 1500);
        productDto.put("description", "A product for testing");
        productDto.put("quantity", 10);
        productDto.put("category", "Electronics");
        productDto.put("status", "Available");
        productDto.put("imageLink", "http://example.com/img.jpg");

        RequestBody body = RequestBody.create(productDto.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/products")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(201, response.code());
            assertNotNull(response.body());
            JsonNode responseBody = mapper.readTree(response.body().string());

            createdProductId = responseBody.get("id").asLong();
            assertTrue(createdProductId > 0);
            assertEquals("Test Product", responseBody.get("title").asText());
        }
    }

    @Test
    @Order(3)
    public void testGetAllProducts() throws IOException {
        Request getAllRequest = new Request.Builder()
                .url(BASE_URL + "/products")
                .get()
                .build();

        try (Response response = client.newCall(getAllRequest).execute()) {
            assertEquals(200, response.code());
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertTrue(responseBody.isArray());
            assertFalse(responseBody.isEmpty());
        }

        Request getByStatusRequest = new Request.Builder()
                .url(BASE_URL + "/products?status=Available")
                .get()
                .build();

        try (Response response = client.newCall(getByStatusRequest).execute()) {
            assertEquals(200, response.code());
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertTrue(responseBody.isArray());
            if (!responseBody.isEmpty()) {
                assertEquals("Available", responseBody.get(0).get("status").asText());
            }
        }
    }

    @Test
    @Order(4)
    public void testGetProductById() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/products/" + createdProductId)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertEquals(createdProductId, responseBody.get("id").asLong());
        }
    }

    @Test
    @Order(5)
    public void testUpdateProduct() throws IOException {
        ObjectNode updatedProduct = mapper.createObjectNode();
        updatedProduct.put("title", "Updated Test Product");
        updatedProduct.put("price", 2000);
        updatedProduct.put("description", "Updated description");
        updatedProduct.put("quantity", 5);
        updatedProduct.put("category", "Electronics");
        updatedProduct.put("status", "popular");
        updatedProduct.put("imageLink", "http://example.com/img2.jpg");

        RequestBody body = RequestBody.create(updatedProduct.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/products/" + createdProductId)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertEquals("Updated Test Product", responseBody.get("title").asText());
        }
    }

    @Test
    @Order(6)
    public void testCartOperations() throws IOException {
        ObjectNode cartItemDto = mapper.createObjectNode();
        cartItemDto.put("productId", createdProductId);
        cartItemDto.put("quantity", 2);

        RequestBody postBody = RequestBody.create(cartItemDto.toString(), JSON);
        Request postReq = new Request.Builder()
                .url(BASE_URL + "/cart/user123")
                .post(postBody)
                .build();

        try (Response response = client.newCall(postReq).execute()) {
            assertEquals(200, response.code());
        }

        Request getReq = new Request.Builder()
                .url(BASE_URL + "/cart/user123")
                .get()
                .build();

        try (Response response = client.newCall(getReq).execute()) {
            assertEquals(200, response.code());
            JsonNode items = mapper.readTree(response.body().string());
            assertTrue(items.isArray());
            assertFalse(items.isEmpty());
        }

        ObjectNode deleteItemDto = mapper.createObjectNode();
        deleteItemDto.put("productId", createdProductId);
        RequestBody deleteBody = RequestBody.create(deleteItemDto.toString(), JSON);

        Request deleteReq = new Request.Builder()
                .url(BASE_URL + "/cart/user123")
                .delete(deleteBody)
                .build();

        try (Response response = client.newCall(deleteReq).execute()) {
            assertEquals(200, response.code());
        }
    }

    @Test
    @Order(7)
    public void testFileUpload() throws IOException {
        byte[] fakeFileBytes = "Dummy file content".getBytes();
        RequestBody body = RequestBody.create(fakeFileBytes, MediaType.get("application/octet-stream"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/upload")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertTrue(responseBody.has("url"));
            assertTrue(responseBody.get("url").asText().contains("shop-bucket"));
        }
    }

    @Test
    @Order(8)
    public void testDeleteProduct() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/products/" + createdProductId)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
        }

        Request verifyReq = new Request.Builder()
                .url(BASE_URL + "/products/" + createdProductId)
                .get()
                .build();

        try (Response response = client.newCall(verifyReq).execute()) {
            assertEquals(404, response.code());
        }
    }

    @Test
    @Order(9)
    public void testCreateProductWithInvalidData() throws IOException {
        ObjectNode invalidProductDto = mapper.createObjectNode();
        invalidProductDto.put("title", ""); // Invalid: title is blank
        invalidProductDto.put("price", -500); // Invalid: negative price
        invalidProductDto.put("description", "A product with invalid data");
        invalidProductDto.put("quantity", -5); // Invalid: negative quantity
        invalidProductDto.put("category", "Electronics");
        invalidProductDto.put("status", "Available");
        invalidProductDto.put("imageLink", "http://example.com/img.jpg");

        RequestBody body = RequestBody.create(invalidProductDto.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/products")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(400, response.code(), "Expected 400 Bad Request for invalid product data");
        }
    }

    @Test
    @Order(10)
    public void testGetNonExistentProduct() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/products/999999999") // ID unlikely to exist
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(404, response.code(), "Expected 404 Not Found for missing product");
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertEquals("Product not found.", responseBody.get("message").asText());
        }
    }

    @Test
    @Order(11)
    public void testMethodNotAllowed() throws IOException {
        // Upload controller only registers POST, so sending GET should fail
        Request request = new Request.Builder()
                .url(BASE_URL + "/upload")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(405, response.code(), "Expected 405 Method Not Allowed");
        }
    }

    @Test
    @Order(12)
    public void testMissingJsonBodyOnPost() throws IOException {
        RequestBody emptyBody = RequestBody.create("", JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/products")
                .post(emptyBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(400, response.code(), "Expected 400 Bad Request due to missing body");
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertTrue(responseBody.has("error"));
        }
    }

    @Test
    @Order(13)
    public void testNonExistentRoute() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/this-route-does-not-exist")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(404, response.code(), "Expected 404 for an unregistered route");
            JsonNode responseBody = mapper.readTree(response.body().string());
            assertEquals("Route not found.", responseBody.get("message").asText());
        }
    }
}