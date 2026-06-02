package com.finalyear.liwatch.cycleswap.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cycle_agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_barter_id")
    private CycleBarter cycleBarter;

    private boolean userASigned;
    private boolean userBSigned;
    private boolean userCSigned;

    private String userAIdCardUrl;
    private String userBIdCardUrl;
    private String userCIdCardUrl;

    private String documentHash;
    private LocalDateTime agreedAt;
}
