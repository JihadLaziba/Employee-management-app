package com.ems.dto;

/**
 * Réponse du login et du renouvellement de session.
 * Le JWT n'est PAS dans le corps : il est envoyé dans un cookie HttpOnly.
 */
public record LoginResponse(
        CurrentUserResponse user,
        long expiresIn
) {}
