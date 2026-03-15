package com.finalyear.liwatch.swapcycle_agreement;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "swap_cycle_agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwapCycleAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cycle_agreement_id")
    private Long cycleAgreementId;

    @Column(name = "swap_cycle_id", nullable = false)
    private Long swapCycleId;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;
}