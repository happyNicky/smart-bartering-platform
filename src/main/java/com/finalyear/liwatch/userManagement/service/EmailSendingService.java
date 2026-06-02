package com.finalyear.liwatch.userManagement.service;

import com.finalyear.liwatch.Notification.Notification;
import com.finalyear.liwatch.Notification.NotificationRepository;
import com.finalyear.liwatch.Notification.enum_notification.Status;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailSendingService {
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @org.springframework.beans.factory.annotation.Value("${liwatch.frontend.url}")
    private String frontendUrl;

    @org.springframework.beans.factory.annotation.Value("${liwatch.backend.url}")
    private String backendUrl;

    public EmailSendingService(JavaMailSender mailSender, NotificationRepository notificationRepository) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
    }



    public void sendVerificationEmail(String email, String token) {

        String link =
                backendUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Verify your account");
        message.setText("Click the link to verify: " + link);

        mailSender.send(message);
    }
    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendUrl + "/auth/reset-password?token=" + token;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Password Reset");
        mail.setText("Click to reset password: " + link);

        mailSender.send(mail);
    }

    @Async
    public void sendNotificationEmailAsync(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getEmailAddress());
            message.setSubject(notification.getSubject());
            message.setText(notification.getBody());
            message.setFrom("noreply@liwatch.com");

            mailSender.send(message);
            notification.setStatus(Status.SENT);
        } catch (Exception e) {
            notification.setStatus(Status.FAILED);
        }
        notificationRepository.save(notification);
    }
}
