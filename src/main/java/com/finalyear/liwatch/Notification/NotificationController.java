package com.finalyear.liwatch.Notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getUserNotifications() {
        List<NotificationDto> dtos = notificationService.getUserNotifications().stream()
                .map(n -> NotificationDto.builder()
                        .id(n.getEmailNotificationId())
                        .subject(n.getSubject())
                        .body(n.getBody())
                        .type(n.getType())
                        .isRead(n.isRead())
                        .sentAt(n.getSentAt())
                        .build())
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> deleteAllNotifications() {
        notificationService.deleteAllNotifications();
        return ResponseEntity.ok().build();
    }
}
