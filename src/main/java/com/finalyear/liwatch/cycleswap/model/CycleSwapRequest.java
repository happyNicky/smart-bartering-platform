package com.finalyear.liwatch.cycleswap.model;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cycle_swap_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleSwapRequest {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator; // User A

    @ManyToOne
    @JoinColumn(name = "middleman_id", nullable = false)
    private User middleman; // User B

    @ManyToOne
    @JoinColumn(name = "closer_id", nullable = false)
    private User closer; // User C

    @ManyToOne
    @JoinColumn(name = "post_a_id", nullable = false)
    private Post postA; // A's post going to C

    @ManyToOne
    @JoinColumn(name = "post_b_id", nullable = false)
    private Post postB; // B's post going to A

    @ManyToOne
    @JoinColumn(name = "post_c_id", nullable = false)
    private Post postC; // C's post going to B

    private boolean middlemanAccepted;
    private boolean closerAccepted;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "cycleSwapRequest", cascade = CascadeType.ALL)
    private CycleBarter cycleBarter;
}
