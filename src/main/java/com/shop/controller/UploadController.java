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
            try {
                if (!"POST".equalsIgnoreCase(method)) {
                    return new HttpResponse(405, "Method Not Allowed", null);
                }

                byte[] fileData = body.getBytes();
                String fileUrl = uploadService.upload(fileData);

                return new HttpResponse(200, "OK",
                        objectMapper.writeValueAsString(Map.of("url", fileUrl)));

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    return new HttpResponse(500, "Internal Server Error",
                            objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
                } catch (Exception ex) {
                    return new HttpResponse(500, "Internal Server Error", null);
                }
            }
        }));
    }
}