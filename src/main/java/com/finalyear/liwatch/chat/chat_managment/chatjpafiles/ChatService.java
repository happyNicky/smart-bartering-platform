package com.finalyear.liwatch.chat.chat_managment.chatjpafiles;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.chat.chat_managment.messagedto.MessageDto;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.negotiation.negotiation_management.NegotiationService;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private NegotiationService negotiationService;

    @Autowired
    private UserUtilService userUtilService;

    // 1. NEW: Autowire the UserRepository so we can fetch the user manually
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Chat saveMessage(MessageDto dto) {

        Negotiation negotiation = negotiationService.getNegotiationById(dto.getNegotiationId());

        // 2. THE FIX: Stop relying on the HTTP Security Context for STOMP WebSockets.
        // We fetch the user directly from the DB using the ID passed from the frontend.
        User sender = userRepository.findById(Long.valueOf(dto.getSenderId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Authorizing only eligible users
        Barter barter = negotiation.getBarter();

        if (!barter.getUserA().getId().equals(sender.getId()) &&
                !barter.getUserB().getId().equals(sender.getId())) {
            throw new RuntimeException("Not allowed");
        }

        Chat chat = Chat.builder()
                .negotiation(negotiation)
                .sender(sender)
                .messageText(dto.getContent())
                .sentAt(LocalDateTime.now()) // Ensures timestamp is never null
                .build();

        // Saving the chat to db and returning the object
        return chatRepository.save(chat);
    }

    public List<Chat> getAllChat(long userId) {
        // This remains untouched.
        // Because /get-all-chat is a standard POST request, the security context works perfectly here.
        userUtilService.checkUser(userId);
        return chatRepository.findChatsByUserId(userId);
    }
}