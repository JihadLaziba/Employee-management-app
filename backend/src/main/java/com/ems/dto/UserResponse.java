package com.ems.dto;

import com.ems.entity.User;
import com.ems.enums.Permission;
import com.ems.enums.Role;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean enabled,
        Set<Permission> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User u) {
        Set<Permission> perms = u.isSuperAdmin()
                ? EnumSet.allOf(Permission.class)
                : (u.getPermissions().isEmpty() ? EnumSet.noneOf(Permission.class) : EnumSet.copyOf(u.getPermissions()));
        return new UserResponse(
                u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isEnabled(),
                perms, u.getCreatedAt(), u.getUpdatedAt());
    }
}
