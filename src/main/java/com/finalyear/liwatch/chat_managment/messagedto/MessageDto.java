package com.finalyear.liwatch.chat_managment.messagedto;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;
import org.aspectj.lang.annotation.RequiredTypes;
import org.springframework.format.annotation.NumberFormat;

@Data
public class MessageDto {
        @NumberFormat
        @NonNull
        private Long negotiationId;
        @NumberFormat
        @NonNull
        private Integer senderId;
        @NonNull
        private String content;
}
