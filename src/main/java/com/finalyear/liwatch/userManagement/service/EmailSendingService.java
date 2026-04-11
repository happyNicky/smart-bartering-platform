package com.finalyear.liwatch.userManagement.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSendingService {
    private final JavaMailSender mailSender;

    public EmailSendingService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendVerificationEmail(String email, String token) {

        String link =
                "http://localhost:8080/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Verify your account");
        message.setText("Click the link to verify: " + link);

        mailSender.send(message);
    }
    public void sendPasswordResetEmail(String to, String token) {
        String link = "http://localhost:8080/api/auth/reset-password?token=" + token; // the link should be password reset page link

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Password Reset");
        mail.setText("Click to reset password: " + link);

        mailSender.send(mail);
    }
}
