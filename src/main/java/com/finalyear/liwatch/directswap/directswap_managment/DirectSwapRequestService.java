package com.finalyear.liwatch.directswap.directswap_managment;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostRepository;
import com.finalyear.liwatch.Post.PostService;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterRepository;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.directswap.dto.CreateDirectSwapRequestDto;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userManagement.service.UserService;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DirectSwapRequestService {
    @Autowired
    private DirectSwapRequestRepository directSwapRequestRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private BarterService barterService;
    @Autowired
    private UserUtilService userUtilService;

    public DirectSwapRequest getSwapRequest(Long id){
        return directSwapRequestRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Swap Request not found!")
        );
    }
    public String makeRequest(CreateDirectSwapRequestDto dto){
        User requestSender=userUtilService.getCurrentlyAuthenticatedUser();
        User requestReceiver=userService.getUser(dto.getReceiverId());
        Post offeredPost= postService.getPostEntity(dto.getOfferedPostId());
        Post requestedPost= postService.getPostEntity(dto.getRequestedPostId());

        DirectSwapRequest directSwapRequest= new DirectSwapRequest();
        directSwapRequest.setRequestSender(requestSender);
        directSwapRequest.setRequestReceiver(requestReceiver);
        directSwapRequest.setOfferedPost(offeredPost);
        directSwapRequest.setStatus(RequestStatus.PENDING);
        directSwapRequest.setRequestedPost(requestedPost);
        directSwapRequest.setCreatedAt(LocalDateTime.now());
        directSwapRequestRepository.save(directSwapRequest);
        return "Request has been sent!";
    }
    @Transactional
    public String acceptRequest(Long id){
        long accepterID= userUtilService.getCurrentlyAuthenticatedUser().getId();
        DirectSwapRequest swapRequest= getSwapRequest(id);
        if (!swapRequest.getRequestReceiver().getId().equals(accepterID)) {
            throw new RuntimeException("You are not allowed to accept this request");
        }
        if (swapRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }
        if (swapRequest.getRequestSender().getId()
                .equals(swapRequest.getRequestReceiver().getId())) {
            throw new RuntimeException("You cannot accept your own request");
        }
        swapRequest.setStatus(RequestStatus.ACCEPTED);
        Barter barter= barterService.createBarter(swapRequest);

        swapRequest.setBarter(barter);
        directSwapRequestRepository.save(swapRequest);

        return "Request Accept";
    }

    public boolean checkRequestMade(long userId,long postId){
        userUtilService.checkUser(userId);
        User user= userService.getUser(userId);
        Post post= postService.getPostEntity(postId);
        return  directSwapRequestRepository.existsByRequestSenderAndOfferedPost(user,post);
    }

    public List<DirectSwapRequest> getMyRequestForSpecificPost(long userId, long postId){
        userUtilService.checkUser(userId);
        User user= userService.getUser(userId);
        Post post= postService.getPostEntity(postId);
        return directSwapRequestRepository.findByRequestReceiverAndRequestedPost(user,post);
    }
    public List<DirectSwapRequest>getAllMyRequest(long userId){
        userUtilService.checkUser(userId);
        User user= userService.getUser(userId);
        return directSwapRequestRepository.findByRequestReceiver(user);
    }
    public List<DirectSwapRequest>getAllRequestSentAndReceived(long userId){
        userUtilService.checkUser(userId);
        User user= userService.getUser(userId);
        return  directSwapRequestRepository.findByRequestReceiverOrRequestSender(user,user);
    }

}
