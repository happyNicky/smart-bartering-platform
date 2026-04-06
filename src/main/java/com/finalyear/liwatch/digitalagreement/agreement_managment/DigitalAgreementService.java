package com.finalyear.liwatch.digitalagreement.agreement_managment;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import com.finalyear.liwatch.digitalagreement.dto.DigitalAgreementDto;
import com.finalyear.liwatch.digitalagreement.dto.InPersonAgreementDto;
import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import com.finalyear.liwatch.digitalagreement.enum_agreement.Status;
import com.finalyear.liwatch.digitalagreement.model.DigitalAgreementModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DigitalAgreementService {
    @Autowired
    private DigitalAgreementRepository repository;
    @Autowired
    private BarterService service;
    public String signPartialAgreement(DigitalAgreementDto dto){
        DigitalAgreement agreement=  new DigitalAgreement();
        Barter barter=  service.getBarter(dto.getBarterId());
        LocalDateTime currentTime= LocalDateTime.now();
        agreement.setBarter(barter);
        agreement.setCreatedAt(currentTime);
        agreement.setStatus(Status.ACTIVE);
        agreement.setUpdatedAt(currentTime);
        agreement.setType(dto.getAgreementType());
        repository.save(agreement);
        return "Agreement created with id: " + agreement.getId();
    }
    public DigitalAgreement completeAgreement(InPersonAgreementDto dto){
        DigitalAgreement agreement= repository.findById(dto.getAgreementID()).orElseThrow(
                ()->new RuntimeException("Partial Agreement is not found!!")
        );
        if(agreement.getType()==AgreementType.FINALIZED)
            throw new RuntimeException("Agreement already finalized");
        agreement.setIdCardImageOfSwapper(dto.getIdCardImageOfSwapper());
        agreement.setType(AgreementType.FINALIZED);
        agreement.setStatus(Status.ACTIVE);
        repository.save(agreement);
        return agreement;
    }
    public String cancelAgreement(Long id){
        DigitalAgreement agreement= repository.findById(id).orElseThrow(
                ()->new RuntimeException("Agreement is not found!")
        );
        agreement.setStatus(Status.CANCELED);
        return "Agreement Canceled !!";
    }
}
