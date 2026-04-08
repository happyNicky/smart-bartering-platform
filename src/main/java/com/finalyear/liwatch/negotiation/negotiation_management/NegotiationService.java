package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NegotiationService {
    @Autowired
    NegotiationRepository negotiationRepository;

    public Negotiation createNegotiation(Barter barter){
        Negotiation negotiation=  new Negotiation();
        negotiation.setBarter(barter);
        negotiation.setStatus(NegotiationStatus.PENDING);
        negotiation.setFairnessScore(null);
        negotiationRepository.save(negotiation);
        return  negotiation;
    }

    public Negotiation getNegotiationById(Long id){

        Negotiation negotiation = negotiationRepository.findById(id)
                .orElseThrow(
                        ()->new RuntimeException("Negotiation not found!")
                );
        return  negotiation;
    }
}
