package com.finalyear.liwatch.Notification;

import com.finalyear.liwatch.Notification.enum_notification.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_notification_id")
    private Long emailNotificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email_address", nullable = false, length = 150)
    private String emailAddress;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private Status status;


}
