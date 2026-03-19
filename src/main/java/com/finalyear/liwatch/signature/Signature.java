package com.finalyear.liwatch.signature;


import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import com.finalyear.liwatch.userManagement.model.User;
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


    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private DigitalAgreement agreement;

    @ManyToOne
    private User user;

    @Column(name = "signature_hash", nullable = false, length = 255)
    private String signatureHash;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;
}
