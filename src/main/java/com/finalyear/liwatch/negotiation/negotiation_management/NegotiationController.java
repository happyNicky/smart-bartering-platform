package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.negotiation.NegotiationResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("negotiation")
public class NegotiationController {
    @Autowired
    private NegotiationService negotiationService;
    @PostMapping("/get-all-nego/{id}")
    public ResponseEntity<java.util.List<com.finalyear.liwatch.negotiation.NegotiationResponseDto>> getAllChat(@PathVariable long id){
        java.util.List<Negotiation> negotiations = negotiationService.getNegotiationByUserId(id);
        
        java.util.List<com.finalyear.liwatch.negotiation.NegotiationResponseDto> responseDtos = negotiations.stream().map(this::toDto)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NegotiationResponseDto> getNegotiation(@PathVariable Long id) {
        Negotiation negotiation = negotiationService.getNegotiationForCurrentUser(id);
        return ResponseEntity.ok(toDto(negotiation));
    }

    @PostMapping("/{id}/fair-value/refresh")
    public ResponseEntity<?> refreshFairValueSuggestion(@PathVariable Long id) {
        try {
            Negotiation negotiation = negotiationService.refreshFairValueSuggestion(id);
            return ResponseEntity.ok(toDto(negotiation));
        } catch (IllegalStateException cooldownException) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Please wait before refreshing again"));
        }
    }

    private com.finalyear.liwatch.negotiation.NegotiationResponseDto toDto(Negotiation nego) {
            com.finalyear.liwatch.barter.dto.BarterResponseDto barterDto = null;
            if (nego.getBarter() != null) {
                barterDto = new com.finalyear.liwatch.barter.dto.BarterResponseDto(
                        nego.getBarter().getId(),
                        nego.getBarter().getCreatedAt(),
                        nego.getBarter().getSwapRequest() != null ? nego.getBarter().getSwapRequest().getId() : null,
                        nego.getBarter().getUserA() != null ? nego.getBarter().getUserA().getId() : null,
                        nego.getBarter().getUserB() != null ? nego.getBarter().getUserB().getId() : null,
                        nego.getBarter().getPostA() != null ? nego.getBarter().getPostA().getPostId() : null,
                        nego.getBarter().getPostB() != null ? nego.getBarter().getPostB().getPostId() : null
                );
            }

            java.util.List<com.finalyear.liwatch.chat.ChatDto> chatDtos = null;
            if (nego.getMessages() != null) {
                chatDtos = nego.getMessages().stream().map(chat -> new com.finalyear.liwatch.chat.ChatDto(
                        chat.getId(),
                        chat.getNegotiation() != null ? chat.getNegotiation().getId() : null,
                        chat.getSender() != null ? chat.getSender().getId() : null,
                        chat.getMessageText(),
                        chat.isEncrypted(),
                        chat.getSentAt()
                )).collect(java.util.stream.Collectors.toList());
            }

            String suggestion = nego.getFairValueSuggestion();
            String badge = parseLine(suggestion, 0);
            String advice = parseLine(suggestion, 1);
            String badgeColor = mapBadgeColor(badge);

            return com.finalyear.liwatch.negotiation.NegotiationResponseDto.builder()
                    .id(nego.getId())
                    .fairnessScore(nego.getFairnessScore())
                    .status(nego.getStatus())
                    .fairValueSuggestion(suggestion)
                    .suggestionUpdatedAt(nego.getSuggestionUpdatedAt())
                    .suggestionBadge(badge)
                    .suggestionBadgeColor(badgeColor)
                    .suggestionAdvice(advice)
                    .barter(barterDto)
                    .messages(chatDtos)
                    .build();
    }
    @GetMapping("/chat/fetch/{id}")
    public void getChatOfNegotiation(@PathVariable("id")Long id){
        negotiationService.getChatsOfNegotiation(id);
    }

    private String parseLine(String suggestion, int idx) {
        if (suggestion == null || suggestion.isBlank()) {
            return "";
        }
        String[] lines = suggestion.replace("\r", "").split("\n");
        if (idx >= lines.length) {
            return "";
        }
        return lines[idx].trim();
    }

    private String mapBadgeColor(String badge) {
        return switch (badge) {
            case "Fair trade" -> "green";
            case "Slightly uneven" -> "yellow";
            case "Uneven trade" -> "red";
            default -> "yellow";
        };
    }
}
