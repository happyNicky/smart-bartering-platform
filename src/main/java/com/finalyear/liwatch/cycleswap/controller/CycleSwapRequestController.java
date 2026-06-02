package com.finalyear.liwatch.cycleswap.controller;

import com.finalyear.liwatch.cycleswap.dto.CreateCycleSwapRequestDto;
import com.finalyear.liwatch.cycleswap.dto.CycleSwapRequestResponseDto;
import com.finalyear.liwatch.cycleswap.service.CycleSwapRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cycle-swap")
public class CycleSwapRequestController {

    @Autowired
    private CycleSwapRequestService service;

    @PostMapping("send-request")
    public ResponseEntity<String> sendRequest(@RequestBody CreateCycleSwapRequestDto dto) {
        return ResponseEntity.ok(service.makeRequest(dto));
    }

    @PostMapping("accept-request/{id}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long id) {
        return ResponseEntity.ok(service.acceptRequest(id));
    }

    @PostMapping("decline-request/{id}")
    public ResponseEntity<String> declineRequest(@PathVariable Long id) {
        return ResponseEntity.ok(service.declineRequest(id));
    }

    @PostMapping("cancel-request/{id}")
    public ResponseEntity<String> cancelRequest(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelRequest(id));
    }

    @GetMapping("my-requests")
    public ResponseEntity<List<CycleSwapRequestResponseDto>> getMyRequests() {
        return ResponseEntity.ok(service.getMyRequests());
    }
}
