package com.ems.controller;

import com.ems.dto.NotificationResponse;
import com.ems.dto.PageResponse;
import com.ems.security.UserPrincipal;
import com.ems.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping
    public PageResponse<NotificationResponse> list(@AuthenticationPrincipal UserPrincipal user,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "15") int size) {
        return notificationService.list(user.getId(), page, size);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal UserPrincipal user) {
        return Map.of("count", notificationService.unreadCount(user.getId()));
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserPrincipal user) {
        notificationService.markAllRead(user.getId());
        return ResponseEntity.noContent().build();
    }
}
