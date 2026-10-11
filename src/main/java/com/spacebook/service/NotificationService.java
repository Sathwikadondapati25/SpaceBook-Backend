package com.spacebook.service;

import com.spacebook.entity.Notification;
import com.spacebook.entity.User;
import com.spacebook.exception.ResourceNotFoundException;
import com.spacebook.repository.NotificationRepository;
import com.spacebook.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notification service for in-app notifications
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(Long userId, String type, String title, String message, String entityType, Long entityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
    }

    @Transactional
    public Notification markAsRead(Long userId, Long notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own notifications as read");
        }

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.countByUserAndIsRead(user, false);
    }
}
