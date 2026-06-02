package com.finalyear.liwatch.Notification;

import com.finalyear.liwatch.Notification.enum_notification.Status;
import com.finalyear.liwatch.userManagement.service.EmailSendingService;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserUtilService userUtilService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailSendingService emailSendingService;

    public NotificationService(NotificationRepository notificationRepository,
                               UserUtilService userUtilService,
                               SimpMessagingTemplate messagingTemplate,
                               EmailSendingService emailSendingService) {
        this.notificationRepository = notificationRepository;
        this.userUtilService = userUtilService;
        this.messagingTemplate = messagingTemplate;
        this.emailSendingService = emailSendingService;
    }

    public List<Notification> getUserNotifications() {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public long getUnreadCount() {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long id) {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        notificationRepository.markAsReadByIdAndUserId(id, userId);
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotification(Long id) {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        notificationRepository.deleteByEmailNotificationIdAndUserId(id, userId);
    }

    @Transactional
    public void deleteAllNotifications() {
        Long userId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        notificationRepository.deleteByUserId(userId);
    }

    public Notification createNotification(Long userId, String emailAddress, String subject, String body, String type) {
        boolean skipEmail = "Message".equals(type) 
                || (type != null && (
                    type.startsWith("SWAP_") 
                    || type.startsWith("BARTER_")
                    || type.startsWith("RATING_")
                    || type.startsWith("BADGE_")
                   ));

        Notification notification = Notification.builder()
                .userId(userId)
                .emailAddress(emailAddress)
                .subject(subject)
                .body(body)
                .type(type)
                .status(skipEmail ? Status.SENT : Status.PENDING)
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

        // Send email asynchronously and immediately
        if (!skipEmail) {
            try {
                emailSendingService.sendNotificationEmailAsync(saved);
            } catch (Exception e) {
                // Log warning, scheduler will pick it up if it stays PENDING
            }
        }

        return saved;
    }
}
