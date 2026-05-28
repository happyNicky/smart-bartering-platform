package com.finalyear.liwatch.digitalagreement.agreement_managment;

import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import com.finalyear.liwatch.digitalagreement.dto.DigitalAgreementDto;
import com.finalyear.liwatch.digitalagreement.dto.InPersonAgreementDto;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dga")
public class DigitalAgreementController {
    @Autowired
    private DigitalAgreementService service;

    @Autowired
    private UserUtilService userUtilService;

    @PostMapping("/sign_partial_agreement")
    public ResponseEntity<String> signPartialAgreement(@Valid @RequestBody DigitalAgreementDto dto) {
        Long currentUserId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        return ResponseEntity.ok(service.signPartialAgreement(dto, currentUserId));
    }

    @PostMapping("/reject_agreement/{barterId}")
    public ResponseEntity<String> rejectAgreement(@PathVariable Long barterId) {
        Long currentUserId = userUtilService.getCurrentlyAuthenticatedUser().getId();
        return ResponseEntity.ok(service.rejectAgreement(barterId, currentUserId));
    }

    @PostMapping("/sign_final_agreement")
    public ResponseEntity<DigitalAgreement> signFinalAgreement(@Valid @RequestBody InPersonAgreementDto dto) {
        return ResponseEntity.ok(service.completeAgreement(dto));
    }

    @PostMapping("/cancel_agreement/{id}")
    public ResponseEntity<String> cancelAgreement(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelAgreement(id));
    }
}
