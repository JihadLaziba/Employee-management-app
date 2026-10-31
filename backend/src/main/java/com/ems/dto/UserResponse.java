package com.ems.dto;

import com.ems.common.TimeUtils;
import com.ems.entity.User;
import com.ems.enums.Permission;
import com.ems.enums.Role;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/** Utilisateur renvoyé au frontend : le mot de passe n'est jamais exposé. */
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean enabled,
        Set<Permission> permissions,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User u) {
        Set<Permission> perms = u.isSuperAdmin()
                ? EnumSet.allOf(Permission.class)
                : (u.getPermissions().isEmpty() ? EnumSet.noneOf(Permission.class) : EnumSet.copyOf(u.getPermissions()));
        return new UserResponse(
                u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isEnabled(),
                perms, TimeUtils.toInstant(u.getCreatedAt()), TimeUtils.toInstant(u.getUpdatedAt()));
    }
}
