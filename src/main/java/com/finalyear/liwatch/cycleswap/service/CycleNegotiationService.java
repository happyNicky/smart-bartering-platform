package com.finalyear.liwatch.cycleswap.service;

import com.finalyear.liwatch.cycleswap.dto.CycleChatDto;
import com.finalyear.liwatch.cycleswap.dto.CycleNegotiationResponseDto;
import com.finalyear.liwatch.cycleswap.dto.CycleSwapRequestResponseDto;
import com.finalyear.liwatch.cycleswap.model.CycleAgreement;
import com.finalyear.liwatch.cycleswap.model.CycleBarter;
import com.finalyear.liwatch.cycleswap.model.CycleChat;
import com.finalyear.liwatch.cycleswap.model.CycleNegotiation;
import com.finalyear.liwatch.cycleswap.repository.CycleAgreementRepository;
import com.finalyear.liwatch.cycleswap.repository.CycleBarterRepository;
import com.finalyear.liwatch.cycleswap.repository.CycleChatRepository;
import com.finalyear.liwatch.cycleswap.repository.CycleNegotiationRepository;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CycleNegotiationService {

    @Autowired
    private CycleNegotiationRepository cycleNegotiationRepository;

    @Autowired
    private CycleBarterRepository cycleBarterRepository;
    
    @Autowired
    private CycleChatRepository cycleChatRepository;

    @Autowired
    private CycleAgreementRepository cycleAgreementRepository;

    @Autowired
    private CycleSwapRequestService cycleSwapRequestService;

    @Autowired
    private UserUtilService userUtilService;

    public List<CycleNegotiationResponseDto> getMyNegotiations() {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        return cycleBarterRepository.findByUserAOrUserBOrUserC(user, user, user)
                .stream()
                .map(barter -> toDto(barter.getCycleNegotiation()))
                .collect(Collectors.toList());
    }

    public CycleNegotiationResponseDto getNegotiationById(Long id) {
        CycleNegotiation neg = cycleNegotiationRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Negotiation not found")
        );
        return toDto(neg);
    }

    @Transactional
    public CycleChatDto sendMessage(Long negotiationId, String message) {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        CycleNegotiation neg = cycleNegotiationRepository.findById(negotiationId).orElseThrow(
            () -> new RuntimeException("Negotiation not found")
        );

        CycleChat chat = new CycleChat();
        chat.setCycleNegotiation(neg);
        chat.setSender(user);
        chat.setMessage(message);
        chat.setSentAt(LocalDateTime.now());
        
        cycleChatRepository.save(chat);

        CycleChatDto dto = new CycleChatDto();
        dto.setId(chat.getId());
        dto.setSenderId(user.getId());
        dto.setSenderName(user.getFullName());
        dto.setMessage(chat.getMessage());
        dto.setSentAt(chat.getSentAt());
        return dto;
    }

    @Transactional
    public String signAgreement(Long barterId) {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        CycleBarter barter = cycleBarterRepository.findById(barterId).orElseThrow(
            () -> new RuntimeException("Barter not found")
        );

        CycleAgreement agreement = cycleAgreementRepository.findAll().stream()
            .filter(a -> a.getCycleBarter().getId().equals(barterId))
            .findFirst().orElseGet(() -> {
                CycleAgreement newAg = new CycleAgreement();
                newAg.setCycleBarter(barter);
                return cycleAgreementRepository.save(newAg);
            });

        if (barter.getUserA().getId().equals(user.getId())) agreement.setUserASigned(true);
        if (barter.getUserB().getId().equals(user.getId())) agreement.setUserBSigned(true);
        if (barter.getUserC().getId().equals(user.getId())) agreement.setUserCSigned(true);

        if (agreement.isUserASigned() && agreement.isUserBSigned() && agreement.isUserCSigned()) {
            agreement.setAgreedAt(LocalDateTime.now());
            try {
                String rawDoc = barterId + ":CYCLE_EXCHANGE:" + LocalDateTime.now();
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(rawDoc.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                agreement.setDocumentHash(hexString.toString());
            } catch (Exception e) {
                agreement.setDocumentHash("CYCLE_AGREEMENT_" + barterId + "_" + System.currentTimeMillis());
            }
            barter.getCycleNegotiation().setStatus(com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus.AGREED);
        }

        cycleAgreementRepository.save(agreement);
        return "Signed successfully";
    }

    @Transactional
    public String submitIdCard(Long barterId, String idCardUrl) {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        CycleBarter barter = cycleBarterRepository.findById(barterId).orElseThrow(
            () -> new RuntimeException("Barter not found")
        );

        CycleAgreement agreement = cycleAgreementRepository.findAll().stream()
            .filter(a -> a.getCycleBarter().getId().equals(barterId))
            .findFirst().orElseThrow(() -> new RuntimeException("Agreement not signed yet"));

        if (barter.getUserA().getId().equals(user.getId())) agreement.setUserAIdCardUrl(idCardUrl);
        if (barter.getUserB().getId().equals(user.getId())) agreement.setUserBIdCardUrl(idCardUrl);
        if (barter.getUserC().getId().equals(user.getId())) agreement.setUserCIdCardUrl(idCardUrl);

        if (agreement.getUserAIdCardUrl() != null && agreement.getUserBIdCardUrl() != null && agreement.getUserCIdCardUrl() != null) {
            // All three uploaded IDs, finalize
            agreement.setAgreedAt(LocalDateTime.now());
            // If hash is not set, set it
            if (agreement.getDocumentHash() == null) {
                try {
                    String rawDoc = barterId + ":CYCLE_FINAL:" + LocalDateTime.now();
                    java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                    byte[] hashBytes = digest.digest(rawDoc.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    StringBuilder hexString = new StringBuilder();
                    for (byte b : hashBytes) {
                        String hex = Integer.toHexString(0xff & b);
                        if (hex.length() == 1) hexString.append('0');
                        hexString.append(hex);
                    }
                    agreement.setDocumentHash(hexString.toString());
                } catch (Exception e) {
                    agreement.setDocumentHash("CYCLE_AGREEMENT_FINAL_" + barterId + "_" + System.currentTimeMillis());
                }
            }
        }

        cycleAgreementRepository.save(agreement);
        return "ID Card uploaded successfully";
    }

    private CycleNegotiationResponseDto toDto(CycleNegotiation neg) {
        if (neg == null) return null;
        CycleNegotiationResponseDto dto = new CycleNegotiationResponseDto();
        dto.setId(neg.getId());
        dto.setCycleBarterId(neg.getCycleBarter().getId());
        dto.setStatus(neg.getStatus());
        dto.setRequestDetails(cycleSwapRequestService.toDto(neg.getCycleBarter().getCycleSwapRequest()));

        List<CycleChatDto> chatDtos = neg.getMessages().stream().map(chat -> {
            CycleChatDto c = new CycleChatDto();
            c.setId(chat.getId());
            c.setSenderId(chat.getSender().getId());
            c.setSenderName(chat.getSender().getFullName());
            c.setMessage(chat.getMessage());
            c.setSentAt(chat.getSentAt());
            return c;
        }).collect(Collectors.toList());

        dto.setMessages(chatDtos);

        CycleAgreement agreement = cycleAgreementRepository.findAll().stream()
            .filter(a -> a.getCycleBarter().getId().equals(neg.getCycleBarter().getId()))
            .findFirst().orElse(null);

        if (agreement != null) {
            dto.setUserASigned(agreement.isUserASigned());
            dto.setUserBSigned(agreement.isUserBSigned());
            dto.setUserCSigned(agreement.isUserCSigned());
            dto.setUserAIdCardUrl(agreement.getUserAIdCardUrl());
            dto.setUserBIdCardUrl(agreement.getUserBIdCardUrl());
            dto.setUserCIdCardUrl(agreement.getUserCIdCardUrl());
            String hash = agreement.getDocumentHash();
            if (agreement.isUserASigned() && agreement.isUserBSigned() && agreement.isUserCSigned()) {
                if (hash == null || hash.startsWith("CYCLE_AGREEMENT_")) {
                    try {
                        String rawDoc = neg.getCycleBarter().getId() + ":CYCLE_EXCHANGE:" + (agreement.getAgreedAt() != null ? agreement.getAgreedAt() : LocalDateTime.now());
                        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                        byte[] hashBytes = digest.digest(rawDoc.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        StringBuilder hexString = new StringBuilder();
                        for (byte b : hashBytes) {
                            String hex = Integer.toHexString(0xff & b);
                            if (hex.length() == 1) hexString.append('0');
                            hexString.append(hex);
                        }
                        hash = hexString.toString();
                        agreement.setDocumentHash(hash);
                        cycleAgreementRepository.save(agreement);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            dto.setDocumentHash(hash);
            
            String agreementType = "PENDING";
            if (agreement.isUserASigned() && agreement.isUserBSigned() && agreement.isUserCSigned()) {
                agreementType = "PARTIAL";
                if (agreement.getUserAIdCardUrl() != null && agreement.getUserBIdCardUrl() != null && agreement.getUserCIdCardUrl() != null) {
                    agreementType = "FINALIZED";
                }
            }
            dto.setAgreementType(agreementType);
        }

        return dto;
    }
}
