package com.finalyear.liwatch.directswap.directswap_managment;

import com.finalyear.liwatch.directswap.dto.CreateDirectSwapRequestDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/direct-swap")
public class DirectSwapRequestController {
    @Autowired
    private DirectSwapRequestService directSwapRequestService;
    @PostMapping("send-request")
    public ResponseEntity<String > sendRequest(@Valid @RequestBody CreateDirectSwapRequestDto dto){
        return ResponseEntity.ok(directSwapRequestService.makeRequest(dto));
    }
    @PostMapping("accept-request/{id}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long id){
        return  ResponseEntity.ok(directSwapRequestService.acceptRequest(id));
    }
}
