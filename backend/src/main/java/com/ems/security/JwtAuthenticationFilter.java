package com.ems.security;

import com.ems.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentifie chaque requête à partir du JWT contenu dans le cookie HttpOnly.
 *
 * L'utilisateur est rechargé depuis la base à chaque requête : si un Super Admin
 * retire une permission ou désactive un compte, c'est effectif immédiatement.
 *
 * Volontairement pas annoté @Component : il est créé dans SecurityConfig pour
 * éviter que Spring Boot l'enregistre une deuxième fois comme filtre servlet.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthCookieService cookieService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            cookieService.read(request)
                    .flatMap(jwtService::extractUserId)
                    .flatMap(userDetailsService::loadUserById)
                    .filter(UserPrincipal::isEnabled)
                    .ifPresent(user -> {
                        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }

        chain.doFilter(request, response);
    }
}
