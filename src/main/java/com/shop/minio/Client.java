package com.shop.minio;

import com.shop.service.VaultService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import java.io.ByteArrayInputStream;

/**
 * Wrapper for MinIO client that handles bucket creation and file uploads.
 */
public class Client {
    private static final String BUCKET_NAME = VaultService.getInstance().getSecret(
            "MINIO_BUCKET_NAME");
    private static final String MINIO_ENDPOINT = VaultService.getInstance().getSecret(
            "MINIO_ENDPOINT");
    private static final String MINIO_ROOT_USER = VaultService.getInstance().getSecret(
            "MINIO_ROOT_USER");
    private static final String MINIO_ROOT_PASSWORD = VaultService.getInstance().getSecret(
            "MINIO_ROOT_PASSWORD");

    private final MinioClient minioClient;

    private Client() {
        this.minioClient = create();
    }

    /**
     * Initializes the MinIO client and ensures the bucket exists. If the bucket does not exist,
     * it will be created.
     *
     * @return Initialized MinIO client ready for use.
     */
    private static MinioClient create() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(MINIO_ENDPOINT)
                .credentials(MINIO_ROOT_USER, MINIO_ROOT_PASSWORD)
                .build();

        try {
            boolean isExist = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(BUCKET_NAME)
                            .build()
            );
            if (!isExist) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(BUCKET_NAME)
                                .build());
            }
        } catch (MinioException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return minioClient;
    }

    /**
     * Provides access to the singleton MinIO client instance.
     *
     * @return Singleton instance of the MinIO client.
     */
    public static Client getMinioClient() {
        return ClientHolder.HOLDER;
    }

    /**
     * Uploads a file to the MinIO bucket.
     *
     * @param fileData Byte array representing the file data to be uploaded.
     * @return URL of the uploaded file in the format: {minioEndpoint}/{bucketName}/{objectName}
     */
    public String upload(byte[] fileData) {
        String objectName = "upload_" + System.currentTimeMillis();

        try {
            this.minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(fileData), (long) fileData.length,
                                    (long) -1)
                            .build()
            );

            return String.format("%s/%s/%s", MINIO_ENDPOINT, BUCKET_NAME, objectName);
        } catch (MinioException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class ClientHolder {
        private static final Client HOLDER = new Client();
    }
}
