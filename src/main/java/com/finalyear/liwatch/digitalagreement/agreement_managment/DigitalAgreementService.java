package com.finalyear.liwatch.digitalagreement.agreement_managment;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import com.finalyear.liwatch.digitalagreement.dto.DigitalAgreementDto;
import com.finalyear.liwatch.digitalagreement.dto.InPersonAgreementDto;
import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import com.finalyear.liwatch.digitalagreement.enum_agreement.Status;
import com.finalyear.liwatch.rating.service.RatingWindowService;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finalyear.liwatch.Notification.NotificationService;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import java.time.LocalDateTime;

@Service
public class DigitalAgreementService {

    private final DigitalAgreementRepository repository;
    private final BarterService barterService;
    private final RatingWindowService ratingWindowService;
    private final com.finalyear.liwatch.Post.PostRepository postRepository;
    private final NotificationService notificationService;
    private final UserUtilService userUtilService;

    public DigitalAgreementService(
            DigitalAgreementRepository repository,
            BarterService barterService,
            RatingWindowService ratingWindowService,
            com.finalyear.liwatch.Post.PostRepository postRepository,
            NotificationService notificationService,
            UserUtilService userUtilService) {
        this.repository = repository;
        this.barterService = barterService;
        this.ratingWindowService = ratingWindowService;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
        this.userUtilService = userUtilService;
    }

    @Transactional
    public String signPartialAgreement(DigitalAgreementDto dto, Long currentUserId) {
        Barter barter = barterService.getBarter(dto.getBarterId());
        
        // Find existing agreement or create new one
        DigitalAgreement agreement = repository.findByBarterId(dto.getBarterId())
                .orElseGet(() -> {
                    DigitalAgreement newAgreement = new DigitalAgreement();
                    newAgreement.setBarter(barter);
                    newAgreement.setCreatedAt(LocalDateTime.now());
                    newAgreement.setStatus(Status.PENDING);
                    newAgreement.setType(dto.getAgreementType());
                    
                    // Generate dynamic default terms
                    String userAName = barter.getUserA() != null ? barter.getUserA().getFullName() : "User A";
                    String userBName = barter.getUserB() != null ? barter.getUserB().getFullName() : "User B";
                    String itemAName = barter.getPostA() != null ? barter.getPostA().getTitle() : "Item A";
                    String itemBName = barter.getPostB() != null ? barter.getPostB().getTitle() : "Item B";
                    
                    String terms = "DIGITAL EXCHANGE AGREEMENT\n\n" +
                            "This agreement is entered into by and between the parties named below for the exchange of items/services on the Smart Bartering Platform.\n\n" +
                            "1. PARTIES & ITEMS:\n" +
                            "   - Party A: " + userAName + " agrees to provide: \"" + itemAName + "\"\n" +
                            "   - Party B: " + userBName + " agrees to provide: \"" + itemBName + "\"\n\n" +
                            "2. TERMS & CONDITIONS:\n" +
                            "   - Both parties agree that the items/services described above are of comparable value and are exchanged voluntarily.\n" +
                            "   - Both parties certify that they own or have the full authority to transfer the respective items/services.\n" +
                            "   - The exchange is binding once both digital signatures are completed.\n\n" +
                            "3. ELECTRONIC SIGNATURE:\n" +
                            "   - By signing, each party accepts the above terms. A cryptographic SHA-256 hash will seal the transaction once finalized.";
                    
                    newAgreement.setAgreementTerms(terms);
                    return newAgreement;
                });

        LocalDateTime now = LocalDateTime.now();
        agreement.setUpdatedAt(now);
        agreement.setType(dto.getAgreementType());

        // Identify which party is signing
        if (barter.getUserA() != null && currentUserId.equals(barter.getUserA().getId())) {
            agreement.setUserASigned(true);
        } else if (barter.getUserB() != null && currentUserId.equals(barter.getUserB().getId())) {
            agreement.setUserBSigned(true);
        } else {
            throw new RuntimeException("User is not authorized to sign this agreement");
        }

        // If both parties have signed, generate the SHA-256 hash and activate the agreement (UC05 confirmed partial agreement)
        if (agreement.isUserASigned() && agreement.isUserBSigned()) {
            agreement.setStatus(Status.ACTIVE);
            agreement.setType(AgreementType.PARTIAL);
            try {
                String rawDoc = barter.getId() + ":" + agreement.getAgreementTerms() + ":" + now;
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
                agreement.setDocumentHash("HASH-ERR-" + System.currentTimeMillis());
            }
        } else {
            // Keep pending if not signed by both yet
            agreement.setStatus(Status.PENDING);
            agreement.setType(AgreementType.PARTIAL);
        }

        repository.save(agreement);

        // Notify parties
        com.finalyear.liwatch.userManagement.model.User signer = 
            (barter.getUserA() != null && barter.getUserA().getId().equals(currentUserId)) ? barter.getUserA() : barter.getUserB();
        com.finalyear.liwatch.userManagement.model.User recipient = 
            (barter.getUserA() != null && barter.getUserA().getId().equals(currentUserId)) ? barter.getUserB() : barter.getUserA();

        if (agreement.getStatus() == Status.ACTIVE) {
            // Notify both A and B
            if (barter.getUserA() != null) {
                notificationService.createNotification(
                        barter.getUserA().getId(),
                        barter.getUserA().getEmail(),
                        "Exchange Agreement Activated",
                        "The exchange agreement for the barter has been signed by both parties and is now ACTIVE.",
                        "Agreement"
                );
            }
            if (barter.getUserB() != null) {
                notificationService.createNotification(
                        barter.getUserB().getId(),
                        barter.getUserB().getEmail(),
                        "Exchange Agreement Activated",
                        "The exchange agreement for the barter has been signed by both parties and is now ACTIVE.",
                        "Agreement"
                );
            }
        } else {
            // Notify recipient that signer has signed
            if (recipient != null && signer != null) {
                notificationService.createNotification(
                        recipient.getId(),
                        recipient.getEmail(),
                        "Exchange Agreement Signed by Partner",
                        signer.getFullName() + " has signed the digital agreement. Please sign to activate it.",
                        "Agreement"
                );
            }
        }

        return "Agreement signed successfully. Current status: " + agreement.getStatus();
    }

    @Transactional
    public String rejectAgreement(Long barterId, Long currentUserId) {
        Barter barter = barterService.getBarter(barterId);
        
        // Only involved parties can reject
        if ((barter.getUserA() == null || !currentUserId.equals(barter.getUserA().getId())) &&
            (barter.getUserB() == null || !currentUserId.equals(barter.getUserB().getId()))) {
            throw new RuntimeException("Unauthorized user");
        }

        DigitalAgreement agreement = repository.findByBarterId(barterId)
                .orElseGet(() -> {
                    DigitalAgreement newAgreement = new DigitalAgreement();
                    newAgreement.setBarter(barter);
                    newAgreement.setCreatedAt(LocalDateTime.now());
                    newAgreement.setStatus(Status.CANCELED);
                    newAgreement.setType(AgreementType.PARTIAL);
                    newAgreement.setAgreementTerms("Agreement proposal rejected by swapper before completion.");
                    return newAgreement;
                });
        
        agreement.setStatus(Status.CANCELED);
        agreement.setUserASigned(false);
        agreement.setUserBSigned(false);
        agreement.setUpdatedAt(LocalDateTime.now());
        
        repository.save(agreement);

        // Cancel the negotiation associated with this barter
        if (barter.getNegotiation() != null) {
            barter.getNegotiation().setStatus(NegotiationStatus.CANCELED);
        }

        com.finalyear.liwatch.userManagement.model.User rejector = 
            (barter.getUserA() != null && barter.getUserA().getId().equals(currentUserId)) ? barter.getUserA() : barter.getUserB();
        com.finalyear.liwatch.userManagement.model.User recipient = 
            (barter.getUserA() != null && barter.getUserA().getId().equals(currentUserId)) ? barter.getUserB() : barter.getUserA();

        if (recipient != null && rejector != null) {
            notificationService.createNotification(
                    recipient.getId(),
                    recipient.getEmail(),
                    "Exchange Agreement Rejected",
                    rejector.getFullName() + " has rejected the digital exchange agreement.",
                    "Agreement"
                );
        }

        return "Agreement rejected successfully";
    }

    /**
     * Physical exchange complete - finalize agreement and open rating window.
     */
    @Transactional
    public DigitalAgreement completeAgreement(InPersonAgreementDto dto) {
        DigitalAgreement agreement = repository.findById(dto.getAgreementID())
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        if (agreement.getType() == AgreementType.FINALIZED) {
            throw new RuntimeException("Agreement already finalized");
        }

        Long currentUserId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        Barter barter = agreement.getBarter();

        if (barter.getUserA() != null && currentUserId.equals(barter.getUserA().getId())) {
            agreement.setUploadedIdByA(dto.getIdCardImageOfSwapper());
        } else if (barter.getUserB() != null && currentUserId.equals(barter.getUserB().getId())) {
            agreement.setUploadedIdByB(dto.getIdCardImageOfSwapper());
        } else {
            throw new RuntimeException("Unauthorized user for physical exchange completion");
        }

        // If both parties have uploaded their respective partner ID card images, set to FINALIZED
        if (agreement.getUploadedIdByA() != null && !agreement.getUploadedIdByA().isBlank()
                && agreement.getUploadedIdByB() != null && !agreement.getUploadedIdByB().isBlank()) {
            
            agreement.setType(AgreementType.FINALIZED);
            agreement.setStatus(Status.ACTIVE);
            agreement.setUpdatedAt(LocalDateTime.now());
            repository.save(agreement);

            // Close both posts involved in the trade automatically
            if (barter.getPostA() != null) {
                barter.getPostA().setStatus(com.finalyear.liwatch.Post.enums.Status.CLOSED);
                postRepository.save(barter.getPostA());
            }
            if (barter.getPostB() != null) {
                barter.getPostB().setStatus(com.finalyear.liwatch.Post.enums.Status.CLOSED);
                postRepository.save(barter.getPostB());
            }

            // trigger: open rating window now that exchange is complete
            ratingWindowService.openWindowForBarter(barter.getId());

            // Notify both parties of finalized agreement
            if (barter.getUserA() != null) {
                notificationService.createNotification(
                        barter.getUserA().getId(),
                        barter.getUserA().getEmail(),
                        "Exchange Completed Successfully",
                        "The exchange has been completed and finalized! You can now rate your barter partner.",
                        "Agreement"
                );
            }
            if (barter.getUserB() != null) {
                notificationService.createNotification(
                        barter.getUserB().getId(),
                        barter.getUserB().getEmail(),
                        "Exchange Completed Successfully",
                        "The exchange has been completed and finalized! You can now rate your barter partner.",
                        "Agreement"
                );
            }
        } else {
            repository.save(agreement);
        }

        return agreement;
    }

    /**
     * Partial agreement cancelled - open rating window for both parties.
     */
    @Transactional
    public String cancelAgreement(Long id) {
        DigitalAgreement agreement = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        agreement.setStatus(Status.CANCELED);
        agreement.setUpdatedAt(LocalDateTime.now());
        repository.save(agreement);

        // Also set negotiation status to CANCELED
        if (agreement.getBarter() != null && agreement.getBarter().getNegotiation() != null) {
            agreement.getBarter().getNegotiation().setStatus(NegotiationStatus.CANCELED);
        }

        // trigger: open rating window after cancellation
        ratingWindowService.openWindowForBarter(agreement.getBarter().getId());

        // Notify both parties of agreement cancellation
        Barter barter = agreement.getBarter();
        if (barter != null) {
            if (barter.getUserA() != null) {
                notificationService.createNotification(
                        barter.getUserA().getId(),
                        barter.getUserA().getEmail(),
                        "Exchange Agreement Cancelled",
                        "The exchange agreement has been cancelled. The rating window is now open for feedback.",
                        "Agreement"
                );
            }
            if (barter.getUserB() != null) {
                notificationService.createNotification(
                        barter.getUserB().getId(),
                        barter.getUserB().getEmail(),
                        "Exchange Agreement Cancelled",
                        "The exchange agreement has been cancelled. The rating window is now open for feedback.",
                        "Agreement"
                );
            }
        }

        return "Agreement cancelled. Rating window opened for both parties.";
    }
}
