package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("negotiation")
public class NegotiationController {
    @Autowired
    private NegotiationService negotiationService;

}
