package com.ems.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Minio minio,
        List<SuperAdmin> superAdmins
) {
    public record Jwt(String secret, long expirationMs) {}

    public record Minio(String endpoint, String accessKey, String secretKey, String bucket) {}

    public record SuperAdmin(String name, String email, String password) {}
}
