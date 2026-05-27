package com.finalyear.liwatch.digitalagreement.agreement_managment;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import com.finalyear.liwatch.digitalagreement.dto.DigitalAgreementDto;
import com.finalyear.liwatch.digitalagreement.dto.InPersonAgreementDto;
import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import com.finalyear.liwatch.digitalagreement.enum_agreement.Status;
import com.finalyear.liwatch.rating.service.RatingWindowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DigitalAgreementService {

    private final DigitalAgreementRepository repository;
    private final BarterService barterService;
    private final RatingWindowService ratingWindowService;

    public DigitalAgreementService(
            DigitalAgreementRepository repository,
            BarterService barterService,
            RatingWindowService ratingWindowService) {
        this.repository = repository;
        this.barterService = barterService;
        this.ratingWindowService = ratingWindowService;
    }

    @Transactional
    public String signPartialAgreement(DigitalAgreementDto dto) {
        Barter barter = barterService.getBarter(dto.getBarterId());
        LocalDateTime now = LocalDateTime.now();
        DigitalAgreement agreement = new DigitalAgreement();
        agreement.setBarter(barter);
        agreement.setCreatedAt(now);
        agreement.setStatus(Status.ACTIVE);
        agreement.setUpdatedAt(now);
        agreement.setType(dto.getAgreementType());
        repository.save(agreement);
        return "Agreement created with id: " + agreement.getId();
    }

    /**
     * UC-06: Physical exchange complete — finalize agreement and open rating window.
     */
    @Transactional
    public DigitalAgreement completeAgreement(InPersonAgreementDto dto) {
        DigitalAgreement agreement = repository.findById(dto.getAgreementID())
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        if (agreement.getType() == AgreementType.FINALIZED) {
            throw new RuntimeException("Agreement already finalized");
        }
        agreement.setIdCardImageOfSwapper(dto.getIdCardImageOfSwapper());
        agreement.setType(AgreementType.FINALIZED);
        agreement.setStatus(Status.ACTIVE);
        agreement.setUpdatedAt(LocalDateTime.now());
        repository.save(agreement);

        // UC-06 trigger: open rating window now that exchange is complete
        ratingWindowService.openWindowForBarter(agreement.getBarter().getId());
        return agreement;
    }

    /**
     * UC-05: Partial agreement cancelled — open rating window for both parties.
     */
    @Transactional
    public String cancelAgreement(Long id) {
        DigitalAgreement agreement = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        agreement.setStatus(Status.CANCELED);
        agreement.setUpdatedAt(LocalDateTime.now());
        repository.save(agreement);

        // UC-05 trigger: open rating window after cancellation
        ratingWindowService.openWindowForBarter(agreement.getBarter().getId());
        return "Agreement cancelled. Rating window opened for both parties.";
    }
}
