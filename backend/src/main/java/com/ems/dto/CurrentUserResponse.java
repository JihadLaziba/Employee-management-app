package com.ems.dto;

import com.ems.security.UserPrincipal;
import com.ems.enums.Permission;
import com.ems.enums.Role;

import java.util.Set;


public record CurrentUserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Set<Permission> permissions
) {
    public static CurrentUserResponse from(UserPrincipal p) {
        return new CurrentUserResponse(p.getId(), p.getName(), p.getEmail(), p.getRole(), p.getPermissions());
    }
}
