package com.finalyear.liwatch.negotiation;

import com.finalyear.liwatch.barter.dto.BarterResponseDto;
import com.finalyear.liwatch.chat.ChatDto;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationResponseDto {
    private Long id;
    private Double fairnessScore;
    private NegotiationStatus status;
    private BarterResponseDto barter;
    private List<ChatDto> messages;
}
