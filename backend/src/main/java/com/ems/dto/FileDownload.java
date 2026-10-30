package com.ems.dto;

public record FileDownload(
        byte[] content,
        String contentType,
        String fileName
) {}
