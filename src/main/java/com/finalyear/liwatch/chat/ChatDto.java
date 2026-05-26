package com.finalyear.liwatch.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDto {
    private Long id;
    private Long negotiationId;
    private Long senderId;
    private String messageText;
    private boolean isEncrypted;
    private LocalDateTime sentAt;
}
