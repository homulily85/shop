package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.ImageStorageService;
import com.shop.service.RateLimiterService;
import com.shop.webserver.HttpFileResponse;
import com.shop.webserver.HttpServer;
import com.shop.webserver.HttpTextResponse;

import java.util.Map;

public class ImageStorageController extends AbstractController {
    private final ImageStorageService imageStorageService;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public ImageStorageController(HttpServer server) {
        super(server);
        this.imageStorageService = ImageStorageService.getInstance();
        this.rateLimiterService = RateLimiterService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/images", ((method, queryParams, pathParams, headers, body) -> {
            if (rateLimiterService.isAllowed(headers.getOrDefault("X-Real-Ip", "unknown-client"))){
                return errorResponse(429, "Too Many Requests", "You are requesting too fast.");
            }

            if (!"POST".equalsIgnoreCase(method)) {
                return errorResponse(405, "Method Not Allowed", "Method not allowed");
            }

            if (body == null || body.length == 0) {
                throw new IllegalArgumentException("Missing file data");
            }

            String fileName = imageStorageService.upload(body);

            return new HttpTextResponse(200, "OK",
                    objectMapper.writeValueAsString(Map.of("fileName", fileName)));
        }));

        server.addRoute("/images/:fileName", ((method, queryParams, pathParams, headers, body) -> {
            if (!"GET".equalsIgnoreCase(method)) {
                return errorResponse(405, "Method Not Allowed", "Method not allowed");
            }

            String fileName = pathParams.get("fileName");
            if (fileName == null || fileName.isEmpty()) {
                return errorResponse(400, "Bad Request", "Missing fileName parameter");
            }

            try {
                byte[] fileBytes = imageStorageService.download(fileName);

                return new HttpFileResponse(200, "OK",
                        Map.of("Content-Type", guessContentType(fileName),
                                "Content-Disposition", "attachment; filename=\"" + fileName + "\""),
                        fileBytes);

            } catch (Exception e) {
                e.printStackTrace();
                return errorResponse(500, "Internal Server Error",
                        e.getMessage() != null ? e.getMessage() : "Failed to read file");
            }
        }));

    }

    private String guessContentType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

    private HttpTextResponse errorResponse(int statusCode, String statusMessage, String error)
            throws Exception {
        return new HttpTextResponse(statusCode, statusMessage,
                objectMapper.writeValueAsString(Map.of("error", error)));
    }
}