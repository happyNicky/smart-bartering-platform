package com.finalyear.liwatch.directswap.directswap_managment;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectSwapRequestRepository  extends JpaRepository<DirectSwapRequest,Long> {

    boolean existsByRequestSenderAndOfferedPost(User requestSender, Post offeredPost);

    List<DirectSwapRequest> findByRequestReceiverAndRequestedPost(User requestReceiver, Post requestedPost);
    List<DirectSwapRequest> findByRequestReceiver(User requestReceiver);

    List<DirectSwapRequest> findByRequestReceiverOrRequestSender(User requestReceiver, User requestSender);
}
