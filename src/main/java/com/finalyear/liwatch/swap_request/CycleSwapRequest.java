package com.finalyear.liwatch.swap_request;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.swapcycle.SwapCycle;
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
public class CycleSwapRequest {

    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne
    private User user1;

    @ManyToOne
    private User user2;

    @ManyToOne
    private User user3;

    // each user offers
    @ManyToOne
    private Post post1;

    @ManyToOne
    private Post post2;

    @ManyToOne
    private Post post3;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;


    @OneToOne(mappedBy = "cycleRequest")
    private SwapCycle swapCycle;
}
