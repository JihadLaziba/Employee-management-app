package com.ems.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        CurrentUserResponse user
) {}
