package com.finalyear.liwatch.directswap.directswap_managment;

import com.finalyear.liwatch.directswap.DirectSwapRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DirectSwapRequestService {
    @Autowired
    private DirectSwapRequestRepository repository;

    public DirectSwapRequest getSwapRequest(Long id){
        return repository.findById(id).orElseThrow(
                ()-> new RuntimeException("Swap Request not found!")
        );
    }

}
