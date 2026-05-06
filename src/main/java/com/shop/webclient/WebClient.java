package com.shop.webclient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebClient {

    private final HttpClient httpClient;

    private WebClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static WebClient getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Sends a GET request to the specified URL.
     */
    public String get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        return executeRequest(request);
    }

    /**
     * Sends a POST request to the specified URL with a JSON body.
     */
    public String post(String url, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        return executeRequest(request);
    }

    private String executeRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                throw new RuntimeException("External service failed with status: " + response.statusCode() + " body: " + response.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to call external service (Network issue)", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw new RuntimeException("External service call was interrupted", e);
        }
    }

    private static class Holder {
        private static final WebClient INSTANCE = new WebClient();
    }
}