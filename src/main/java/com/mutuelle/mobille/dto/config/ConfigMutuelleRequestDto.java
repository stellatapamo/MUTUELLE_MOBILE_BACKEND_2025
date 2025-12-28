package com.mutuelle.mobille.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigMutuelleRequestDto {

    @NotNull(message = "Le montant des frais d'inscription est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    @Digits(integer = 12, fraction = 2, message = "Format du montant invalide")
    private BigDecimal registrationFeeAmount;

    @NotNull(message = "Le taux d'intérêt du prêt est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le taux ne peut pas être négatif")
    @Digits(integer = 3, fraction = 2, message = "Format du taux invalide (ex: 3.00)")
    private BigDecimal loanInterestRatePercent;

    @NotNull(message = "")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    @Digits(integer = 12, fraction = 2, message = "Format du montant invalide")
    private BigDecimal agapeAmount;
}