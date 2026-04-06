package com.finalyear.liwatch.negotiation.negotiation_management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NegotiationService {
    @Autowired
    NegotiationRepository repository;
}
