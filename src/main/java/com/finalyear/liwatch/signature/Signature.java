package com.finalyear.liwatch.signature;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signature_id")
    private Long signatureId;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "signature_hash", nullable = false, length = 255)
    private String signatureHash;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;
}
