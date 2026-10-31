package com.ems.service;

import com.ems.entity.AuditLog;
import com.ems.enums.AuditAction;
import com.ems.repository.AuditLogRepository;
import com.ems.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    public static final String USER = "USER";
    public static final String EMPLOYEE = "EMPLOYEE";

    private static final int MAX_DETAILS = 500;

    private final AuditLogRepository repository;

    public void record(AuditAction action, String targetType, Long targetId, String targetName, String details) {
        currentUser().ifPresentOrElse(
                actor -> record(actor, action, targetType, targetId, targetName, details),
                () -> log.warn("Audit skipped, no authenticated user for action {}", action));
    }

    public void record(AuditAction action, String targetType, Long targetId, String targetName) {
        record(action, targetType, targetId, targetName, null);
    }

    public void record(UserPrincipal actor, AuditAction action,
                       String targetType, Long targetId, String targetName, String details) {
        try {
            repository.save(AuditLog.builder()
                    .actorId(actor.getId())
                    .actorName(actor.getName())
                    .actorEmail(actor.getEmail())
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetName(targetName)
                    .details(truncate(details))
                    .build());
        } catch (Exception e) {
            log.error("Could not record audit log for action {}", action, e);
        }
    }

    private java.util.Optional<UserPrincipal> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return java.util.Optional.of(principal);
        }
        return java.util.Optional.empty();
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_DETAILS) {
            return value;
        }
        return value.substring(0, MAX_DETAILS);
    }
}
