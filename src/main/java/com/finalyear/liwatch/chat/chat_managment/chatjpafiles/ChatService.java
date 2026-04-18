package com.finalyear.liwatch.chat.chat_managment.chatjpafiles;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.chat.chat_managment.message_model.MessageModel;
import com.finalyear.liwatch.chat.chat_managment.messagedto.MessageDto;
import com.finalyear.liwatch.negotiation.Negotiation;

import com.finalyear.liwatch.negotiation.negotiation_management.NegotiationRepository;
import com.finalyear.liwatch.negotiation.negotiation_management.NegotiationService;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    private  ChatRepository chatRepository;
    @Autowired
    private NegotiationService negotiationService;
    @Autowired
    private UserUtilService userUtilService;
    public Chat saveMessage(MessageDto dto) {

        Negotiation negotiation=negotiationService.getNegotiationById(dto.getNegotiationId());

        User sender = userUtilService.getCurrentlyAuthenticatedUser();

        //authorizing only eligible users
        Barter barter = negotiation.getBarter();

        if (!barter.getUserA().getId().equals(sender.getId()) &&
                !barter.getUserB().getId().equals(sender.getId())) {
            throw new RuntimeException("Not allowed");
        }

        Chat chat = Chat.builder()
                .negotiation(negotiation)
                .sender(sender)
                .messageText(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();
        //saving the chat to db and returning the object
        return chatRepository.save(chat);
    }
    public List<Chat> getAllChat(long userId){
        userUtilService.checkUser(userId);
        return  chatRepository.findChatsByUserId(userId);
    }
}
