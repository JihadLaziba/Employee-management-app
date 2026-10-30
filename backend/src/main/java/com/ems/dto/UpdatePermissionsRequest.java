package com.ems.dto;

import com.ems.enums.Permission;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdatePermissionsRequest(
        @NotNull(message = "Permissions are required")
        Set<Permission> permissions
) {}
