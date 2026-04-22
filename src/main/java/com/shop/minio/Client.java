package com.shop.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import java.io.ByteArrayInputStream;

public class Client {
    private static final String bucketName = System.getenv("MINIO_BUCKET_NAME");
    private static final String minioEndpoint = System.getenv("MINIO_ENDPOINT");
    private final MinioClient minioClient;

    private Client() {
        this.minioClient = create();
    }

    private static MinioClient create() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(System.getenv("MINIO_ROOT_USER"), System.getenv("MINIO_ROOT_PASSWORD"))
                .build();

        try {
            boolean isExist = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            if (!isExist) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
            }
        } catch (MinioException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return minioClient;
    }

    public static Client getMinioClient() {
        return ClientHolder.HOLDER;
    }

    public String upload(byte[] fileData) {
        String objectName = "upload_" + System.currentTimeMillis();

        try {
            this.minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(fileData), (long) fileData.length,
                                    (long) -1)
                            .build()
            );

            return String.format("%s/%s/%s", minioEndpoint, bucketName, objectName);
        } catch (MinioException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class ClientHolder {
        private static final Client HOLDER = new Client();
    }
}
