package com.ems.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.dto.NotificationResponse;
import com.ems.dto.PageResponse;
import com.ems.entity.User;
import com.ems.repository.AuditLogRepository;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(Long userId, int page, int size) {
        LocalDateTime seenAt = seenAt(userId);
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        return PageResponse.from(
                auditLogRepository.findAllByOrderByCreatedAtDescIdDesc(pageable)
                        .map(a -> NotificationResponse.from(a, userId, seenAt)));
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        LocalDateTime seenAt = seenAt(userId);
        return auditLogRepository.countUnread(seenAt != null ? seenAt : LocalDateTime.of(1970, 1, 1, 0, 0), userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        userRepository.markNotificationsSeen(userId, LocalDateTime.now());
    }

    private LocalDateTime seenAt(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getNotificationsSeenAt();
    }
}
