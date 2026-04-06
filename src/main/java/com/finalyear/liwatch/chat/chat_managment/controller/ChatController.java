package com.finalyear.liwatch.chat.chat_managment.controller;

import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.chat.chat_managment.chatjpafiles.ChatService;
import com.finalyear.liwatch.chat.chat_managment.messagedto.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private ChatService service;
    @MessageMapping("/chat.send")
    public void sendMessage(MessageDto messageDto){
        Chat chat= service.saveMessage(messageDto);
        template.convertAndSend( "/barter/" + messageDto.getNegotiationId(),
                chat);
    }
}
