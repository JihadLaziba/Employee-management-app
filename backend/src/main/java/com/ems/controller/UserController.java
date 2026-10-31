package com.ems.controller;

import com.ems.service.AuditService;
import com.ems.enums.AuditAction;
import com.ems.dto.*;
import com.ems.security.UserPrincipal;
import com.ems.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Gestion des utilisateurs : réservée aux Super Administrateurs.
 * La vérification est faite côté backend par @PreAuthorize,
 * pas seulement en cachant les boutons dans le frontend.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuditService auditService;

    /** GET /api/users?search=&page=0&size=10&sortBy=createdAt&direction=desc */
    @GetMapping
    public PageResponse<UserResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return userService.search(search, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.create(request);
        auditService.record(AuditAction.USER_CREATED, AuditService.USER, created.id(), created.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateUserRequest request,
                               @AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse updated = userService.update(id, request, currentUser.getId());
        auditService.record(AuditAction.USER_UPDATED, AuditService.USER, updated.id(), updated.name());
        return updated;
    }

    /** PUT /api/users/{id}/permissions  body: { "permissions": ["READ", "CREATE"] } */
    @PutMapping("/{id}/permissions")
    public UserResponse updatePermissions(@PathVariable Long id,
                                          @Valid @RequestBody UpdatePermissionsRequest request) {
        UserResponse updated = userService.updatePermissions(id, request.permissions());
        auditService.record(AuditAction.PERMISSIONS_UPDATED, AuditService.USER, updated.id(), updated.name(),
                updated.permissions().isEmpty() ? "No permissions" : updated.permissions().toString());
        return updated;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal currentUser) {
        String name = userService.getById(id).name();
        userService.delete(id, currentUser.getId());
        auditService.record(AuditAction.USER_DELETED, AuditService.USER, id, name);
        return ResponseEntity.noContent().build();
    }
}
