package com.finalyear.liwatch.negotiation;


import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "negotiations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negotiation {

    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barter_id")
    private Barter barter;

    private Double fairnessScore;

    @Enumerated(EnumType.STRING)
    private NegotiationStatus status;

    //  one negotiation will have many messages
    @OneToMany(mappedBy = "negotiation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Chat> messages;


}