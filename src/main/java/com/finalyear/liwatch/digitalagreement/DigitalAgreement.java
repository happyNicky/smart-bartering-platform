package com.finalyear.liwatch.digitalagreement;


import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import com.finalyear.liwatch.digitalagreement.enum_agreement.Status;
import com.finalyear.liwatch.signature.Signature;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "digital_agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class DigitalAgreement {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Barter barter;

    @Enumerated(EnumType.STRING)
    private AgreementType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "id_card_image_of_swapper",nullable = true)
    private  String idCardImageOfSwapper;



}