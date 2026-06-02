package com.finalyear.liwatch.admin;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminAgreementLookupResponse {
    private Long agreementId;
    private String documentHash;
    private String agreementTerms;
    private String type;
    private String status;
    private boolean userASigned;
    private boolean userBSigned;
    private String uploadedIdByA;
    private String uploadedIdByB;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Barter info
    private Long barterId;
    private Long userAId;
    private String userAName;
    private String userAEmail;
    private Long userBId;
    private String userBName;
    private String userBEmail;
    private Long postAId;
    private String postATitle;
    private Long postBId;
    private String postBTitle;
}
