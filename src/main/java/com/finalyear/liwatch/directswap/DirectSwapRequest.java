package com.finalyear.liwatch.directswap;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
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
    private User sender;

    // receiver User B
    @ManyToOne
    private User receiver;

    // what sender offers
    @ManyToOne
    private Post offeredPost;

    // what sender want
    @ManyToOne
    private Post requestedPost;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "swapRequest")
    private Barter barter;
}