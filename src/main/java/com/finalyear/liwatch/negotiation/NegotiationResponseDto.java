package com.finalyear.liwatch.negotiation;

import com.finalyear.liwatch.barter.dto.BarterResponseDto;
import com.finalyear.liwatch.chat.ChatDto;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationResponseDto {
    private Long id;
    private Double fairnessScore;
    private NegotiationStatus status;
    private String fairValueSuggestion;
    private LocalDateTime suggestionUpdatedAt;
    private String suggestionBadge;
    private String suggestionBadgeColor;
    private String suggestionAdvice;
    private BarterResponseDto barter;
    private List<ChatDto> messages;
}
