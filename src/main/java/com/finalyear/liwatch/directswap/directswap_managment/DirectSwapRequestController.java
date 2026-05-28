package com.finalyear.liwatch.directswap.directswap_managment;

import com.finalyear.liwatch.directswap.dto.CreateDirectSwapRequestDto;
import com.finalyear.liwatch.directswap.dto.DirectSwapRequestResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/direct-swap")
public class DirectSwapRequestController {
    @Autowired
    private DirectSwapRequestService directSwapRequestService;

    @PostMapping("send-request")
    public ResponseEntity<String> sendRequest(@Valid @RequestBody CreateDirectSwapRequestDto dto){
        return ResponseEntity.ok(directSwapRequestService.makeRequest(dto));
    }

    @PostMapping("accept-request/{id}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long id){
        return ResponseEntity.ok(directSwapRequestService.acceptRequest(id));
    }

    @PostMapping("check-request/{userId}/{postId}")
    public ResponseEntity<Boolean> checkRequest(@PathVariable long userId, @PathVariable long postId){
        return ResponseEntity.ok(directSwapRequestService.checkRequestMade(userId, postId));
    }

    @GetMapping("get-request/{id}")
    public ResponseEntity<DirectSwapRequestResponseDto> getRequest(@PathVariable Long id){
        return ResponseEntity.ok(directSwapRequestService.getRequestById(id));
    }



    @GetMapping("my-requests")
    public ResponseEntity<List<DirectSwapRequestResponseDto>> getMyRequests() {
        return ResponseEntity.ok(directSwapRequestService.getMyRequests());
    }



    @GetMapping("get-all-request/{id}")
    public ResponseEntity<List<DirectSwapRequestResponseDto>> getAllRequestSentAndReceived(@PathVariable long id){
        return ResponseEntity.ok(directSwapRequestService.getAllRequestSentAndReceived(id));
    }
}