package com.finalyear.liwatch.digitalagreement.dto;

import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalAgreementDto {

        @NotNull
        private long barterId;

        @NotNull
        private AgreementType agreementType;

        // --- THE MISSING FIELDS FOR THE FRONTEND ---
        private String status;
        private Boolean userASigned;
        private Boolean userBSigned;
        private String documentHash;
        private String agreementTerms;
}