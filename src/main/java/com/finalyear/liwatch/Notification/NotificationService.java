package com.finalyear.liwatch.Notification;

import com.finalyear.liwatch.Notification.enum_notification.Status;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserUtilService userUtilService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               UserUtilService userUtilService,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userUtilService = userUtilService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Notification> getUserNotifications() {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public long getUnreadCount() {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long id) {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        List<Notification> unread = notificationRepository.findByUserIdOrderBySentAtDesc(userId)
                .stream().filter(n -> !n.isRead()).toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public Notification createNotification(Long userId, String emailAddress, String subject, String body, String type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .emailAddress(emailAddress)
                .subject(subject)
                .body(body)
                .type(type)
                .status(Status.PENDING)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        
        Notification saved = notificationRepository.save(notification);

        // Push real-time notification
        NotificationDto dto = NotificationDto.builder()
                .id(saved.getEmailNotificationId())
                .subject(saved.getSubject())
                .body(saved.getBody())
                .type(saved.getType())
                .isRead(saved.isRead())
                .sentAt(saved.getSentAt())
                .build();
        
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);

        return saved;
    }
}
