package com.finalyear.liwatch.chat.chat_managment.controller;

import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.chat.chat_managment.chatjpafiles.ChatService;
import com.finalyear.liwatch.chat.chat_managment.messagedto.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private ChatService chatService;
    @MessageMapping("/chat.send")
    public void sendMessage(MessageDto messageDto){
        Chat chat= chatService.saveMessage(messageDto);
        template.convertAndSend( "/barter/" + messageDto.getNegotiationId(),
                chat);
    }
    @PostMapping("/get-all-chat/{id}")
    public ResponseEntity<List<Chat>> getAllChat(@PathVariable long id){
        return ResponseEntity.ok(chatService.getAllChat(id));
    }
}
