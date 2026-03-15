package com.finalyear.liwatch.swapcycle;

import com.finalyear.liwatch.swapcycle.enum_swapcycle.Status;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "swap_cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwapCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "swap_cycle_id")
    private Long swapCycleId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "fairness_score", precision = 3, scale = 2)
    private BigDecimal fairnessScore;


}