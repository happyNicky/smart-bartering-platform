package com.finalyear.liwatch.barter.barter_managment;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.post_management.PostService;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.dto.BarterCreateDto;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.directswap.directswap_managment.DirectSwapRequestService;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BarterService {
    @Autowired
    private BarterRepository repository;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private DirectSwapRequestService swapRequestService;


    public Barter getBarter(Long id){
        return repository.findById(id).orElseThrow(
                ()-> new RuntimeException("Barter Not found!")
        );

    }

    public String createBarter(BarterCreateDto dto){
        Barter barter = new Barter();
        User userA = userService.getUser(dto.getUserAId());
        User userB = userService.getUser(dto.getUserBId());
        Post postA= postService.getPost(dto.getPostAId());
        Post postB= postService.getPost(dto.getPostBId());
        LocalDateTime currentTime= LocalDateTime.now();
        DirectSwapRequest request= swapRequestService.getSwapRequest(dto.getSwapRequestId());
        barter.setPostA(postA);
        barter.setPostB(postB);
        barter.setUserA(userA);
        barter.setUserB(userB);
        barter.setSwapRequest(request);
        barter.setCreatedAt(currentTime);
        repository.save(barter);
        return "Barter Created Successfully!";
    }

}
