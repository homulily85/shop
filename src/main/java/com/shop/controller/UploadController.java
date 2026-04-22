package com.shop.controller;

import com.shop.service.UploadService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

public class UploadController extends AbstractController {
    private final UploadService uploadService;

    public UploadController(HttpServer server) {
        super(server);
        uploadService = UploadService.getInstance();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/upload", ((method, queryParams, pathParams, headers, body) -> {
            if (!"POST".equalsIgnoreCase(method)) {
                return new HttpResponse(405, "Method Not Allowed", "{\"error\":\"Only POST is " +
                        "supported\"}");
            }
            try {
                byte[] fileData = body.getBytes();
                String fileUrl = uploadService.upload(fileData);
                return new HttpResponse(200, "OK", "{\"url\": \"%s\"}".formatted(fileUrl));
            } catch (Exception e) {
                e.printStackTrace();
                return new HttpResponse(500, "Internal Server Error",
                        "{\"error\": \"%s\"}".formatted(e.getMessage()));
            }
        }));
    }
}
