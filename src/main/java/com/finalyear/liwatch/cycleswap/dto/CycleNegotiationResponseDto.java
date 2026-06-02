package com.finalyear.liwatch.cycleswap.dto;

import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import lombok.Data;
import java.util.List;

@Data
public class CycleNegotiationResponseDto {
    private Long id;
    private Long cycleBarterId;
    private NegotiationStatus status;
    private CycleSwapRequestResponseDto requestDetails;
    private List<CycleChatDto> messages;
    private boolean userASigned;
    private boolean userBSigned;
    private boolean userCSigned;
    private String userAIdCardUrl;
    private String userBIdCardUrl;
    private String userCIdCardUrl;
    private String documentHash;
    private String agreementType;
}
