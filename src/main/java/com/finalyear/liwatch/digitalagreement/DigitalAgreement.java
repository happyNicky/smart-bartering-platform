package com.finalyear.liwatch.digitalagreement;


import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import com.finalyear.liwatch.digitalagreement.enum_agreement.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "digital_agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @Column(name = "barter_id", nullable = false)
    private Long barterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type")
    private AgreementType agreementType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;



}