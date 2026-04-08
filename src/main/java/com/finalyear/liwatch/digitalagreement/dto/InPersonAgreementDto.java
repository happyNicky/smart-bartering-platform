package com.finalyear.liwatch.digitalagreement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InPersonAgreementDto {

    @NotNull
    private  Long agreementID;
    @NotNull
    @NotBlank
    private String idCardImageOfSwapper;
}
