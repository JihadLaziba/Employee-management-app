package com.ems.security;

import com.ems.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthCookieService {

    private static final String PATH = "/api";

    private final String name;
    private final boolean secure;
    private final Duration maxAge;

    public AuthCookieService(AppProperties props) {
        this.name = props.cookie().name();
        this.secure = props.cookie().secure();
        this.maxAge = Duration.ofMillis(props.jwt().expirationMs());
    }

    public String getName() {
        return name;
    }

    public ResponseCookie create(String token) {
        return base(token).maxAge(maxAge).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }

    public boolean isPresent(HttpServletRequest request) {
        return read(request).isPresent();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(PATH);
    }
}
