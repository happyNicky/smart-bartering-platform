package com.finalyear.liwatch.media.agreement_media;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agreement_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreementMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_media_id")
    private Long agreementMediaId;

    @Column(name = "participant1_id", nullable = false)
    private Long participant1Id;

    @Column(name = "participant2_id", nullable = false)
    private Long participant2Id;

    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;

    @Column(name = "image_of_participant1", nullable = false, length = 255)
    private String imageOfParticipant1;

    @Column(name = "image_of_participant2", nullable = false, length = 255)
    private String imageOfParticipant2;
}
