package com.finalyear.liwatch.negotiation;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "negotiations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "negotiation_id")
    private Long negotiationId;

    @Column(name = "barter_id")
    private Long barterId;

    @Column(name = "fairness_score", precision = 3, scale = 2)
    private BigDecimal fairnessScore;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ACTIVE
    }
}