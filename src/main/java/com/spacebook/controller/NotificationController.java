package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.entity.Notification;
import com.spacebook.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification controller
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long notificationId,
                                                                Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Notification notification = notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
