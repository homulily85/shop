package com.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.service.UploadService;
import com.shop.webserver.HttpResponse;
import com.shop.webserver.HttpServer;

import java.util.Map;

public class UploadController extends AbstractController {
    private final UploadService uploadService;
    private final ObjectMapper objectMapper;

    public UploadController(HttpServer server) {
        super(server);
        this.uploadService = UploadService.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void registerRoutes() {
        server.addRoute("/upload", ((method, queryParams, pathParams, headers, body) -> {
            if (!"POST".equalsIgnoreCase(method)) {
                return new HttpResponse(405, "Method Not Allowed", null);
            }

            if (body == null || body.length == 0) {
                throw new IllegalArgumentException("Missing file data");
            }

            String fileUrl = uploadService.upload(body);

            return new HttpResponse(200, "OK",
                    objectMapper.writeValueAsString(Map.of("url", fileUrl)));
        }));
    }
}