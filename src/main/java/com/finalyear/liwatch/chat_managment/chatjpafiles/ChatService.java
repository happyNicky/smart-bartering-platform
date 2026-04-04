package com.finalyear.liwatch.chat_managment.chatjpafiles;

import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.chat_managment.message_model.MessageModel;
import com.finalyear.liwatch.chat_managment.messagedto.MessageDto;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.test_repositories.NegotiationRepository;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class ChatService {
    @Autowired
    private  ChatRepository chatRepository;
    @Autowired
    private  NegotiationRepository negotiationRepository;
    @Autowired
    private  UserRepository userRepository;

    public Chat saveMessage(MessageDto dto) {
        MessageModel message= new MessageModel();
        message.setSenderId(dto.getSenderId());
        message.setContent(dto.getContent());
        message.setNegotiationId(dto.getNegotiationId());
        Negotiation negotiation = negotiationRepository.findById(dto.getNegotiationId())
                .orElseThrow();

        User sender = userRepository.findById(Long.valueOf(dto.getSenderId()))
                .orElseThrow();

        Chat chat = Chat.builder()
                .negotiation(negotiation)
                .sender(sender)
                .messageText(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();


        return chatRepository.save(chat);
    }
}
