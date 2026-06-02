package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.chat.chat_managment.chatjpafiles.ChatRepository;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NegotiationService {
    private final NegotiationRepository negotiationRepository;
    private final UserUtilService userUtilService;
    private final FairValueSuggestionService fairValueSuggestionService;
    private final ChatRepository chatRepository;

    public NegotiationService(
            NegotiationRepository negotiationRepository,
            UserUtilService userUtilService,
            FairValueSuggestionService fairValueSuggestionService,
            ChatRepository chatRepository) {
        this.negotiationRepository = negotiationRepository;
        this.userUtilService = userUtilService;
        this.fairValueSuggestionService = fairValueSuggestionService;
        this.chatRepository = chatRepository;
    }

    public Negotiation createNegotiation(Barter barter){
        Negotiation negotiation=  new Negotiation();
        negotiation.setBarter(barter);
        negotiation.setStatus(NegotiationStatus.PENDING);
        negotiation.setFairnessScore(null);
        fairValueSuggestionService.ensureSuggestion(negotiation);
        negotiationRepository.save(negotiation);
        return  negotiation;
    }
    public List<Negotiation> getNegotiationByUserId(Long userId){
        userUtilService.checkUser(userId);
        return negotiationRepository.findUserNegotiations(userId);
    }
    public Negotiation getNegotiationById(Long id){
        return negotiationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negotiation not found!"));
    }
    @Transactional
    public List<Chat> getChatsOfNegotiation(Long id){
        Negotiation negotiation = getNegotiationById(id);
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        boolean modified = false;
        if (negotiation.getMessages() != null) {
            for (Chat chat : negotiation.getMessages()) {
                if (chat.getSender() != null && !chat.getSender().getId().equals(currentUser.getId())) {
                    if (!chat.isRead()) {
                        chat.setRead(true);
                        modified = true;
                    }
                }
            }
        }
        if (modified) {
            chatRepository.saveAll(negotiation.getMessages());
        }
        return negotiation.getMessages();
    }

    @Transactional
    public Negotiation getNegotiationForCurrentUser(Long negotiationId) {
        Negotiation negotiation = getNegotiationById(negotiationId);
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        Long currentUserId = currentUser.getId();

        Long userA = negotiation.getBarter().getUserA().getId();
        Long userB = negotiation.getBarter().getUserB().getId();
        if (!currentUserId.equals(userA) && !currentUserId.equals(userB)) {
            throw new RuntimeException("Unauthorized access");
        }
        boolean modified = false;
        if (negotiation.getMessages() != null) {
            for (Chat chat : negotiation.getMessages()) {
                if (chat.getSender() != null && !chat.getSender().getId().equals(currentUserId)) {
                    if (!chat.isRead()) {
                        chat.setRead(true);
                        modified = true;
                    }
                }
            }
        }
        if (modified) {
            chatRepository.saveAll(negotiation.getMessages());
        }
        fairValueSuggestionService.ensureSuggestion(negotiation);
        return negotiationRepository.save(negotiation);
    }

    @Transactional
    public Negotiation refreshFairValueSuggestion(Long negotiationId) {
        Negotiation negotiation = getNegotiationForCurrentUser(negotiationId);
        fairValueSuggestionService.refreshSuggestion(negotiation);
        return negotiationRepository.save(negotiation);
    }

}
