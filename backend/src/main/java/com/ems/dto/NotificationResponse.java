package com.ems.dto;

import com.ems.common.TimeUtils;
import com.ems.entity.AuditLog;
import com.ems.enums.AuditAction;

import java.time.Instant;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String actorName,
        String actorEmail,
        AuditAction action,
        String targetType,
        Long targetId,
        String targetName,
        String details,
        String message,
        Instant createdAt,
        boolean own,
        boolean unread
) {
    public static NotificationResponse from(AuditLog a, Long currentUserId, LocalDateTime seenAt) {
        boolean own = a.getActorId() != null && a.getActorId().equals(currentUserId);
        boolean unread = !own && (seenAt == null || a.getCreatedAt().isAfter(seenAt));
        return new NotificationResponse(
                a.getId(),
                a.getActorName(),
                a.getActorEmail(),
                a.getAction(),
                a.getTargetType(),
                a.getTargetId(),
                a.getTargetName(),
                a.getDetails(),
                message(a),
                TimeUtils.toInstant(a.getCreatedAt()),
                own,
                unread);
    }

    private static String message(AuditLog a) {
        String target = a.getTargetName() != null ? a.getTargetName() : "an item";
        return switch (a.getAction()) {
            case LOGIN -> "signed in";
            case LOGOUT -> "signed out";
            case SESSION_EXPIRED -> "was signed out after inactivity";
            case USER_CREATED -> "created the user " + target;
            case USER_UPDATED -> "updated the user " + target;
            case USER_DELETED -> "deleted the user " + target;
            case PERMISSIONS_UPDATED -> "changed the permissions of " + target;
            case EMPLOYEE_CREATED -> "added the employee " + target;
            case EMPLOYEE_UPDATED -> "updated the employee " + target;
            case EMPLOYEE_DELETED -> "deleted the employee " + target;
            case PHOTO_UPLOADED -> "uploaded a photo for " + target;
            case CV_UPLOADED -> "uploaded the CV of " + target;
            case CV_ACCESSED -> "opened the CV of " + target;
            case CONTRACT_GENERATED -> "generated the contract of " + target;
        };
    }
}
