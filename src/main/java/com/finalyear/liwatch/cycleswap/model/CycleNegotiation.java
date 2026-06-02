package com.finalyear.liwatch.cycleswap.model;

import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cycle_negotiations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleNegotiation {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_barter_id")
    private CycleBarter cycleBarter;

    @Enumerated(EnumType.STRING)
    private NegotiationStatus status;

    @OneToMany(mappedBy = "cycleNegotiation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CycleChat> messages;
}
