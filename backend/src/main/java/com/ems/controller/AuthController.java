package com.ems.controller;

import com.ems.service.AuditService;
import com.ems.enums.AuditAction;
import com.ems.dto.CurrentUserResponse;
import com.ems.dto.LoginRequest;
import com.ems.dto.LoginResponse;
import com.ems.security.AuthCookieService;
import com.ems.security.JwtService;
import com.ems.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthCookieService cookieService;
    private final AuditService auditService;

    /** POST /api/auth/login : vérifie email + mot de passe, place le JWT dans un cookie HttpOnly. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));

        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        auditService.record(user, AuditAction.LOGIN, null, null, null, null);
        return withNewToken(user);
    }

    /**
     * POST /api/auth/refresh : prolonge la session d'un utilisateur actif.
     * Appelé par le frontend quand l'utilisateur travaille, ou clique sur "Rester connecté".
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@AuthenticationPrincipal UserPrincipal user) {
        return withNewToken(user);
    }

    /**
     * POST /api/auth/logout : supprime le cookie.
     * ?reason=idle quand la déconnexion vient du minuteur d'inactivité.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal user,
                                       @RequestParam(required = false) String reason) {
        if (user != null) {
            auditService.record(user, "idle".equals(reason) ? AuditAction.SESSION_EXPIRED : AuditAction.LOGOUT,
                    null, null, null, null);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieService.clear().toString())
                .build();
    }

    /** GET /api/auth/me : renvoie l'utilisateur connecté (permissions à jour). */
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal UserPrincipal user) {
        return CurrentUserResponse.from(user);
    }

    private ResponseEntity<LoginResponse> withNewToken(UserPrincipal user) {
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.create(token).toString())
                .body(new LoginResponse(CurrentUserResponse.from(user), jwtService.getExpirationMs()));
    }
}
