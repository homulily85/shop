package com.shop.service;

import com.shop.minio.Client;

public class UploadService {
    private final Client minioClient = Client.getMinioClient();

    private UploadService() {
    }

    /**
     * Provides access to the singleton instance of UploadService.
     * @return Singleton instance of UploadService.
     */
    public static UploadService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Upload a file to MinIO and return the URL of the uploaded file.
     * @param fileData Byte array representing the file data to be uploaded.
     * @return URL of the uploaded file in MinIO.
     */
    public String upload(byte[] fileData) {
        return minioClient.upload(fileData);
    }

    private static class Holder {
        private static final UploadService INSTANCE = new UploadService();
    }

}
