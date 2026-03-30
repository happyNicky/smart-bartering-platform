package com.finalyear.liwatch.chat_managment.message_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageModel {
    private Long negotiationId;
    private Integer senderId;
    private String content;
}
