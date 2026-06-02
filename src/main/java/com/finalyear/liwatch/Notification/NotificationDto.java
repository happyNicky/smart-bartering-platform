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
    @com.fasterxml.jackson.annotation.JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime sentAt;

    public boolean getIsRead() {
        return this.isRead;
    }

    public boolean isRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
