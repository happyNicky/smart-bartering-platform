package com.finalyear.liwatch.swapcycle;

import com.finalyear.liwatch.swap_cycle_request.CycleSwapRequest;
import com.finalyear.liwatch.swapcycle.enum_swapcycle.Status;
import com.finalyear.liwatch.swapcycle_agreement.CycleAgreement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "swap_cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SwapCycle {

    @Id @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "request_id")
    private CycleSwapRequest cycleRequest;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Double fairnessScore;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "swapCycle")
    private List<CycleAgreement> agreements;
}