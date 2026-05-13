package com.shop.minio;

import com.shop.service.VaultService;
import io.minio.*;
import io.minio.errors.MinioException;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
     * Uploads an image file to the MinIO bucket.
     *
     * @param fileData Byte array representing the file data to be uploaded.
     * @return The name of newly uploaded file on server.
     * @throws IllegalArgumentException if the file is not an image.
     */
    public String upload(byte[] fileData) {
        Tika tika = new Tika();
        String mimeTypeString = tika.detect(fileData);

        if (mimeTypeString == null || !mimeTypeString.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed. Detected: " + mimeTypeString);
        }

        String extension;
        try {
            MimeType mimeType = MimeTypes.getDefaultMimeTypes().forName(mimeTypeString);
            extension = mimeType.getExtension();
        } catch (Exception e) {
            extension = ".bin";
        }

        String objectName = System.currentTimeMillis() + extension;

        try {
            this.minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(fileData), (long) fileData.length, (long) -1)
                            .contentType(mimeTypeString)
                            .build()
            );

            return objectName;

        } catch (MinioException e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while uploading to MinIO", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a file from the MinIO bucket as an InputStream.
     *
     * @param objectName The exact name of the file on the MinIO server.
     * @return InputStream of the file.
     * @throws RuntimeException if the file cannot be retrieved.
     */
    public InputStream getFileStream(String objectName) {
        try {
            return this.minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .build()
            );
        } catch (MinioException e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while retrieving file from MinIO: " + objectName, e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class ClientHolder {
        private static final Client HOLDER = new Client();
    }
}
