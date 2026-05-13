package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.ImageStorageService;
import com.shop.webserver.HttpFileResponse;
import com.shop.webserver.HttpServer;
import com.shop.webserver.HttpTextResponse;

import java.util.Map;

public class ImageStorageController extends AbstractController {
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;

    public ImageStorageController(HttpServer server) {
        super(server);
        this.imageStorageService = ImageStorageService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/images", ((method, queryParams, pathParams, headers, body) -> {
            if (!"POST".equalsIgnoreCase(method)) {
                return new HttpTextResponse(405, "Method Not Allowed", null);
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
                return new HttpTextResponse(405, "Method Not Allowed", null);
            }

            String fileName = pathParams.get("fileName");
            if (fileName == null || fileName.isEmpty()) {
                return new HttpTextResponse(400, "Bad Request: Missing 'name' parameter", null);
            }

            try {
                byte[] fileBytes = imageStorageService.download(fileName);

                return new HttpFileResponse(200, "OK",
                        Map.of("Content-Type", guessContentType(fileName),
                                "Content-Disposition", "attachment; filename=\"" + fileName + "\""),
                        fileBytes);

            } catch (Exception e) {
                return new HttpTextResponse(500, "Internal Server Error", null);
            }
        }));

    }

    private String guessContentType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}