package com.shop.service;

import com.shop.minio.Client;

public class UploadService {
    private final Client minioClient = Client.getMinioClient();

    private UploadService() {
    }

    public static UploadService getInstance() {
        return Holder.INSTANCE;
    }

    public String upload(byte[] fileData) {
        return minioClient.upload(fileData);
    }

    private static class Holder {
        private static final UploadService INSTANCE = new UploadService();
    }

}
