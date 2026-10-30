package com.ems.service;

import com.ems.common.exception.StorageException;
import com.ems.config.AppProperties;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Accès bas niveau à MinIO (protocole S3).
 * Le bucket est privé : les fichiers ne sont jamais accessibles directement,
 * ils passent toujours par l'API, qui vérifie le JWT et les permissions.
 */
@Slf4j
@Service
public class StorageService {

    private final MinioClient minioClient;
    private final String bucket;

    public StorageService(MinioClient minioClient, AppProperties props) {
        this.minioClient = minioClient;
        this.bucket = props.minio().bucket();
    }

    /** Crée le bucket au démarrage s'il n'existe pas. */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            } else {
                log.info("MinIO bucket ready: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Could not initialize MinIO bucket '{}'", bucket, e);
        }
    }

    public void upload(String key, byte[] content, String contentType) {
        try (InputStream in = new ByteArrayInputStream(content)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, content.length, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Upload failed for " + key, e);
        }
    }

    public byte[] download(String key) {
        try (GetObjectResponse in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build())) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new StorageException("Download failed for " + key, e);
        }
    }

    /** Suppression "best effort" : un échec est journalisé mais ne bloque pas l'opération. */
    public void deleteQuietly(String key) {
        if (key == null) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            log.warn("Could not delete object {} from MinIO", key, e);
        }
    }
}
