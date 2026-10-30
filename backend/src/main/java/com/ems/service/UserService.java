package com.ems.service;

import com.ems.common.PageableFactory;
import com.ems.common.exception.BusinessException;
import com.ems.common.exception.ResourceNotFoundException;
import com.ems.dto.*;
import com.ems.entity.User;
import com.ems.enums.Permission;
import com.ems.enums.Role;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "email", "role", "enabled", "createdAt");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String search, int page, int size, String sortBy, String direction) {
        var pageable = PageableFactory.of(page, size, sortBy, direction, SORTABLE_FIELDS, "createdAt");
        return PageResponse.from(
                userRepository.search(PageableFactory.normalizeSearch(search), pageable).map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserResponse.from(findOrThrow(id));
    }

    public UserResponse create(CreateUserRequest req) {
        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw BusinessException.conflict("A user with this email already exists");
        }

        User user = User.builder()
                .name(req.name().trim())
                .email(email)
                .password(passwordEncoder.encode(req.password()))
                .role(req.role())
                .enabled(req.enabled() == null || req.enabled())
                .build();

        if (req.role() == Role.USER && req.permissions() != null) {
            user.getPermissions().addAll(req.permissions());
        }

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(Long id, UpdateUserRequest req, Long currentUserId) {
        User user = findOrThrow(id);
        String email = normalizeEmail(req.email());

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw BusinessException.conflict("A user with this email already exists");
        }

        if (Objects.equals(id, currentUserId)) {
            if (req.role() != Role.SUPER_ADMIN) {
                throw BusinessException.badRequest("You cannot remove your own super administrator role");
            }
            if (!req.enabled()) {
                throw BusinessException.badRequest("You cannot disable your own account");
            }
        }

        boolean losesSuperAdminAccess = user.isSuperAdmin() && (req.role() != Role.SUPER_ADMIN || !req.enabled());
        if (losesSuperAdminAccess) {
            ensureNotLastActiveSuperAdmin(user);
        }

        user.setName(req.name().trim());
        user.setEmail(email);
        user.setRole(req.role());
        user.setEnabled(req.enabled());

        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        if (req.role() == Role.SUPER_ADMIN) {
            user.getPermissions().clear();
        }

        return UserResponse.from(user);
    }

    public UserResponse updatePermissions(Long id, Set<Permission> permissions) {
        User user = findOrThrow(id);

        if (user.isSuperAdmin()) {
            throw BusinessException.badRequest("Super administrators already have all permissions");
        }

        user.getPermissions().clear();
        user.getPermissions().addAll(permissions);
        return UserResponse.from(user);
    }

    public void delete(Long id, Long currentUserId) {
        User user = findOrThrow(id);

        if (Objects.equals(id, currentUserId)) {
            throw BusinessException.badRequest("You cannot delete your own account");
        }
        if (user.isSuperAdmin()) {
            ensureNotLastActiveSuperAdmin(user);
        }

        userRepository.delete(user);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void ensureNotLastActiveSuperAdmin(User user) {
        if (user.isEnabled() && userRepository.countByRoleAndEnabledTrue(Role.SUPER_ADMIN) <= 1) {
            throw BusinessException.badRequest("At least one active super administrator is required");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
