package com.finalyear.liwatch.digitalagreement.model;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import com.finalyear.liwatch.signature.Signature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalAgreementModel {
    private Barter barter;
    private AgreementType agreementType;
    private List<Signature> signatures;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String idOfSwapper;

}
