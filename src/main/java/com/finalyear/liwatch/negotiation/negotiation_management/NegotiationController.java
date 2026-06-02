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
@RequestMapping("/api/negotiation")
public class NegotiationController {
    @Autowired
    private NegotiationService negotiationService;

    @Autowired
    private com.finalyear.liwatch.digitalagreement.agreement_managment.DigitalAgreementRepository digitalAgreementRepository;

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
                com.finalyear.liwatch.barter.Barter barter = nego.getBarter();
                com.finalyear.liwatch.userManagement.DTO.UserSummeryDto userADto = com.finalyear.liwatch.userManagement.DTO.UserSummeryDto.from(barter.getUserA());
                com.finalyear.liwatch.userManagement.DTO.UserSummeryDto userBDto = com.finalyear.liwatch.userManagement.DTO.UserSummeryDto.from(barter.getUserB());
                com.finalyear.liwatch.Post.PostResponseDto postADto = barter.getPostA() != null ?
                        com.finalyear.liwatch.Post.utils.PostUtilMethods.getPostResponseDtoFromPost(barter.getUserA(), barter.getPostA(), java.util.Collections.emptyList()) : null;
                com.finalyear.liwatch.Post.PostResponseDto postBDto = barter.getPostB() != null ?
                        com.finalyear.liwatch.Post.utils.PostUtilMethods.getPostResponseDtoFromPost(barter.getUserB(), barter.getPostB(), java.util.Collections.emptyList()) : null;

                barterDto = com.finalyear.liwatch.barter.dto.BarterResponseDto.builder()
                        .id(barter.getId())
                        .createdAt(barter.getCreatedAt())
                        .swapRequestId(barter.getSwapRequest() != null ? barter.getSwapRequest().getId() : null)
                        .userA(userADto)
                        .userB(userBDto)
                        .postA(postADto)
                        .postB(postBDto)
                        .build();
            }

            java.util.List<com.finalyear.liwatch.chat.ChatDto> chatDtos = null;
            if (nego.getMessages() != null) {
                chatDtos = nego.getMessages().stream().map(chat -> com.finalyear.liwatch.chat.ChatDto.builder()
                        .id(chat.getId())
                        .negotiationId(chat.getNegotiation() != null ? chat.getNegotiation().getId() : null)
                        .senderId(chat.getSender() != null ? chat.getSender().getId() : null)
                        .messageText(chat.getMessageText())
                        .isEncrypted(chat.isEncrypted())
                        .isRead(chat.isRead())
                        .fileUrl(chat.getFileUrl())
                        .fileName(chat.getFileName())
                        .fileType(chat.getFileType())
                        .sentAt(chat.getSentAt())
                        .build()
                ).collect(java.util.stream.Collectors.toList());
            }

            com.finalyear.liwatch.digitalagreement.dto.DigitalAgreementDto agreementDto = null;
            if (nego.getBarter() != null) {
                java.util.Optional<com.finalyear.liwatch.digitalagreement.DigitalAgreement> agreementOpt =
                        digitalAgreementRepository.findByBarterId(nego.getBarter().getId());
                if (agreementOpt.isPresent()) {
                    com.finalyear.liwatch.digitalagreement.DigitalAgreement agreement = agreementOpt.get();
                    agreementDto = new com.finalyear.liwatch.digitalagreement.dto.DigitalAgreementDto(
                            agreement.getBarter().getId(),
                            agreement.getType(),
                            agreement.getStatus() != null ? agreement.getStatus().name() : null,
                            agreement.isUserASigned(),
                            agreement.isUserBSigned(),
                            agreement.getDocumentHash(),
                            agreement.getAgreementTerms(),
                            agreement.getUploadedIdByA(),
                            agreement.getUploadedIdByB(),
                            agreement.getId()
                    );
                }
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
                    .agreement(agreementDto)
                    .build();
    }
    @GetMapping("/chat/fetch/{id}")
    public ResponseEntity<java.util.List<com.finalyear.liwatch.chat.ChatDto>> getChatOfNegotiation(@PathVariable("id")Long id){
        java.util.List<Chat> chats = negotiationService.getChatsOfNegotiation(id);
        java.util.List<com.finalyear.liwatch.chat.ChatDto> chatDtos = chats.stream().map(chat -> com.finalyear.liwatch.chat.ChatDto.builder()
                .id(chat.getId())
                .negotiationId(chat.getNegotiation() != null ? chat.getNegotiation().getId() : null)
                .senderId(chat.getSender() != null ? chat.getSender().getId() : null)
                .messageText(chat.getMessageText())
                .isEncrypted(chat.isEncrypted())
                .isRead(chat.isRead())
                .fileUrl(chat.getFileUrl())
                .fileName(chat.getFileName())
                .fileType(chat.getFileType())
                .sentAt(chat.getSentAt())
                .build()
        ).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(chatDtos);
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
