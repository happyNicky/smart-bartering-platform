package com.finalyear.liwatch.cycleswap.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CycleChatDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String message;
    private LocalDateTime sentAt;
}
