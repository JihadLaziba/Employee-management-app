package com.ems.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Regroupe toute la configuration "app.*" de application.yml.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cookie cookie,
        Minio minio,
        List<SuperAdmin> superAdmins
) {
    public record Jwt(String secret, long expirationMs) {}

    /**
     * Cookie qui transporte le JWT.
     * secure = true en production (HTTPS), false en local (http://localhost).
     */
    public record Cookie(String name, boolean secure) {}

    public record Minio(String endpoint, String accessKey, String secretKey, String bucket) {}

    public record SuperAdmin(String name, String email, String password) {}
}
