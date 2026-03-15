package com.finalyear.liwatch.swapcycle_participant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "swap_cycle_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwapCycleParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @Column(name = "swap_cycle_id", nullable = false)
    private Long swapCycleId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "offered_post_id", nullable = false)
    private Long offeredPostId;

    @Column(name = "requested_post_id", nullable = false)
    private Long requestedPostId;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "confirmed")
    private Boolean confirmed = false;
}