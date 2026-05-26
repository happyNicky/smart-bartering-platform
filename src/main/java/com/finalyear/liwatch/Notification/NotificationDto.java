package com.finalyear.liwatch.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private String subject;
    private String body;
    private String type;
    private boolean isRead;
    private LocalDateTime sentAt;
}
