package com.finalyear.liwatch.cycleswap.controller;

import com.finalyear.liwatch.cycleswap.dto.CycleChatDto;
import com.finalyear.liwatch.cycleswap.dto.CycleNegotiationResponseDto;
import com.finalyear.liwatch.cycleswap.service.CycleNegotiationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/cycle-negotiation")
public class CycleNegotiationController {

    @Autowired
    private CycleNegotiationService service;

    @GetMapping("my-negotiations")
    public ResponseEntity<List<CycleNegotiationResponseDto>> getMyNegotiations() {
        return ResponseEntity.ok(service.getMyNegotiations());
    }

    @GetMapping("{id}")
    public ResponseEntity<CycleNegotiationResponseDto> getNegotiationById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getNegotiationById(id));
    }

    @PostMapping("{id}/messages")
    public ResponseEntity<CycleChatDto> sendMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.sendMessage(id, body.get("message")));
    }

    @PostMapping("sign-agreement/{barterId}")
    public ResponseEntity<String> signAgreement(@PathVariable Long barterId) {
        return ResponseEntity.ok(service.signAgreement(barterId));
    }

    @PostMapping("submit-id/{barterId}")
    public ResponseEntity<String> submitIdCard(@PathVariable Long barterId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.submitIdCard(barterId, body.get("idCardUrl")));
    }
}
