package com.shop.service;

import com.shop.minio.Client;

import java.io.InputStream;

public class ImageStorageService {
    private final Client minioClient = Client.getMinioClient();

    private ImageStorageService() {
    }

    /**
     * Provides access to the singleton instance of UploadService.
     * @return Singleton instance of UploadService.
     */
    public static ImageStorageService getInstance() {
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

    /**
     * Downloads a file from MinIO and converts it to a byte array.
     * @param fileName The name of the file in MinIO.
     * @return Byte array of the file data.
     */
    public byte[] download(String fileName) {
        try (InputStream stream = minioClient.getFileStream(fileName)) {
            return stream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    private static class Holder {
        private static final ImageStorageService INSTANCE = new ImageStorageService();
    }

}
