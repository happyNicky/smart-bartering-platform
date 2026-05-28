package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.barter.Barter;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminBarterResponse {

    private Long barterId;
    private LocalDateTime createdAt;

    // users
    private Long userAId;
    private String userAName;
    private Long userBId;
    private String userBName;

    // posts being swapped
    private Long postAId;
    private String postATitle;
    private Long postBId;
    private String postBTitle;

    // negotiation snapshot
    private Long negotiationId;
    private String negotiationStatus;
    private Double fairnessScore;

    // agreement snapshot
    private int totalAgreements;
    private long signedAgreements;

    public static AdminBarterResponse from(Barter barter) {
        AdminBarterResponseBuilder builder = AdminBarterResponse.builder()
                .barterId(barter.getId())
                .createdAt(barter.getCreatedAt());

        if (barter.getUserA() != null) {
            builder.userAId(barter.getUserA().getId())
                   .userAName(barter.getUserA().getFullName());
        }
        if (barter.getUserB() != null) {
            builder.userBId(barter.getUserB().getId())
                   .userBName(barter.getUserB().getFullName());
        }
        if (barter.getPostA() != null) {
            builder.postAId(barter.getPostA().getPostId())
                   .postATitle(barter.getPostA().getTitle());
        }
        if (barter.getPostB() != null) {
            builder.postBId(barter.getPostB().getPostId())
                   .postBTitle(barter.getPostB().getTitle());
        }
        if (barter.getNegotiation() != null) {
            builder.negotiationId(barter.getNegotiation().getId())
                   .negotiationStatus(barter.getNegotiation().getStatus() != null
                           ? barter.getNegotiation().getStatus().name() : null)
                   .fairnessScore(barter.getNegotiation().getFairnessScore());
        }
        if (barter.getAgreements() != null) {
            builder.totalAgreements(barter.getAgreements().size())
                   .signedAgreements(barter.getAgreements().stream()
                           .filter(a -> a.isUserASigned() && a.isUserBSigned())
                           .count());
        }

        return builder.build();
    }
}
