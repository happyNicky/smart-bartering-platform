package com.finalyear.liwatch.cycleswap.model;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cycle_barters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleBarter {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "cycle_request_id")
    private CycleSwapRequest cycleSwapRequest;

    @ManyToOne
    private User userA;

    @ManyToOne
    private User userB;

    @ManyToOne
    private User userC;

    @ManyToOne
    private Post postA;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post postB;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post postC;

    @OneToOne(mappedBy = "cycleBarter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private CycleNegotiation cycleNegotiation;
}
