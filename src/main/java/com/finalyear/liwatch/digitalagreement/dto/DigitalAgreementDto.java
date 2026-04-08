package com.finalyear.liwatch.digitalagreement.dto;

import com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType;
import com.finalyear.liwatch.signature.Signature;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.format.annotation.NumberFormat;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalAgreementDto {
        @NotNull
        private  long barterId;
        @NotNull
        private AgreementType agreementType;

}
