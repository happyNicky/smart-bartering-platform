package com.finalyear.liwatch.barter.barter_managment;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostService;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.dto.BarterCreateDto;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.directswap.directswap_managment.DirectSwapRequestService;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.negotiation.negotiation_management.NegotiationService;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BarterService {
    @Autowired
    private BarterRepository barterRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private NegotiationService negotiationService;


    public Barter getBarter(Long id){
        return barterRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Barter Not found!")
        );

    }

    public Barter createBarter(DirectSwapRequest swapRequest){
        Barter barter = new Barter();
        User userA = swapRequest.getRequestSender();
        User userB = swapRequest.getRequestReceiver();
        Post postA= swapRequest.getOfferedPost();
        Post postB= swapRequest.getRequestedPost();
//        DirectSwapRequest request= swapRequestService.getSwapRequest(dto.getSwapRequestId());
        barter.setPostA(postA);
        barter.setPostB(postB);
        barter.setUserA(userA);
        barter.setUserB(userB);
        barter.setSwapRequest(swapRequest);
        barter.setCreatedAt(LocalDateTime.now());
        Negotiation negotiation=negotiationService.createNegotiation(barter);
        barter.setNegotiation(negotiation);
        barterRepository.save(barter);
        return barter;
    }
    public List<Barter> getBarterByUserId(long userId){
        User user = userService.getUser(userId);
        return barterRepository.getBartersByUserAOrUserB(user,user);
    }

}
