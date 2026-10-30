package com.ems.dto;

/** Fichier lu depuis MinIO, prêt à être renvoyé au navigateur. */
public record FileDownload(
        byte[] content,
        String contentType,
        String fileName
) {}
