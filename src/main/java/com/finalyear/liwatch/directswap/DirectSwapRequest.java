package com.finalyear.liwatch.directswap;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectSwapRequest {

    @Id
    @GeneratedValue
    private Long id;

    // sender User A

    @ManyToOne
    @JoinColumn(name = "request_sender_id",nullable = false)
    private User requestSender;

    // receiver User B
    @ManyToOne
    @JoinColumn(name = "request_receiver_id",nullable = false)
    private User requestReceiver;

    // what sender offers
    @ManyToOne
    @JoinColumn(name = "offered_post_id",nullable = false)
    private Post offeredPost;

    // what sender want
    @ManyToOne
    @JoinColumn(name = "requested_post_id",nullable = false)
    private Post requestedPost;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "swapRequest", cascade = CascadeType.ALL)
    private Barter barter;
}