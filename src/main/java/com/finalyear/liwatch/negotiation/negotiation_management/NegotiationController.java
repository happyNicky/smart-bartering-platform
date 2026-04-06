package com.finalyear.liwatch.negotiation.negotiation_management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("negotiation")
public class NegotiationController {
    @Autowired
    private NegotiationService service;
}
