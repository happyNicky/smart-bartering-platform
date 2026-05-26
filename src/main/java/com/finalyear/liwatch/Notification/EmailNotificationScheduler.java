package com.finalyear.liwatch.Notification;

import com.finalyear.liwatch.Notification.enum_notification.Status;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailNotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public EmailNotificationScheduler(NotificationRepository notificationRepository, JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    @Scheduled(fixedRate = 60000)
    public void processPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findByStatus(Status.PENDING);
        
        for (Notification notification : pendingNotifications) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(notification.getEmailAddress());
                message.setSubject(notification.getSubject());
                message.setText(notification.getBody());
                message.setFrom("noreply@liwatch.com"); // optional depending on mail setup
                
                mailSender.send(message);
                
                notification.setStatus(Status.SENT);
            } catch (Exception e) {
                // Ignore errors to let it retry or mark failed
                notification.setStatus(Status.FAILED);
            }
            notificationRepository.save(notification);
        }
    }
}
