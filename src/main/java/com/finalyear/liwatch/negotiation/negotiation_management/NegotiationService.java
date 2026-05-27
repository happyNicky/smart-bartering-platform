package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class NegotiationService {
    @Autowired
    NegotiationRepository negotiationRepository;
    @Autowired
    UserUtilService userUtilService;

    public Negotiation createNegotiation(Barter barter){
        Negotiation negotiation=  new Negotiation();
        negotiation.setBarter(barter);
        negotiation.setStatus(NegotiationStatus.PENDING);
        negotiation.setFairnessScore(null);
        negotiationRepository.save(negotiation);
        return  negotiation;
    }
    public List<Negotiation> getNegotiationByUserId(Long userId){
        userUtilService.checkUser(userId);
        return negotiationRepository.findUserNegotiations(userId);
    }
    public Negotiation getNegotiationById(Long id){

        Negotiation negotiation = negotiationRepository.findById(id)
                .orElseThrow(
                        ()->new RuntimeException("Negotiation not found!")
                );
        return  negotiation;
    }
    @Transactional
    public List<Chat> getChatsOfNegotiation(Long id){
        Negotiation negotiation =getNegotiationById(id);
        return negotiation.getMessages();
    }

}
