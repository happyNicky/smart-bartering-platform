package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("negotiation")
public class NegotiationController {
    @Autowired
    private NegotiationService negotiationService;
    @PostMapping("/get-all-nego/{id}")
    public ResponseEntity<java.util.List<com.finalyear.liwatch.negotiation.NegotiationResponseDto>> getAllChat(@PathVariable long id){
        java.util.List<Negotiation> negotiations = negotiationService.getNegotiationByUserId(id);
        
        java.util.List<com.finalyear.liwatch.negotiation.NegotiationResponseDto> responseDtos = negotiations.stream().map(nego -> {
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

            return com.finalyear.liwatch.negotiation.NegotiationResponseDto.builder()
                    .id(nego.getId())
                    .fairnessScore(nego.getFairnessScore())
                    .status(nego.getStatus())
                    .barter(barterDto)
                    .messages(chatDtos)
                    .build();
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }
    @GetMapping("/chat/fetch/{id}")
    public void getChatOfNegotiation(@PathVariable("id")Long id){
        negotiationService.getChatsOfNegotiation(id);
    }
}
