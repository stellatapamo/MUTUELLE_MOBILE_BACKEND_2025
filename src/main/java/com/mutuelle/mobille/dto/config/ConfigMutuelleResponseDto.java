package com.mutuelle.mobille.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigMutuelleResponseDto {

    private Long id;

    private BigDecimal registrationFeeAmount;

    private BigDecimal loanInterestRatePercent;

    private BigDecimal agapeAmount;

    private LocalDateTime updatedAt;

    private String updatedBy;

    public ConfigMutuelleResponseDto(com.mutuelle.mobille.models.MutuelleConfig config) {
        this.id = config.getId();
        this.registrationFeeAmount = config.getRegistrationFeeAmount();
        this.loanInterestRatePercent = config.getLoanInterestRatePercent();
        this.agapeAmount = config.getAgapeAmount();
        this.updatedAt = config.getUpdatedAt();
        this.updatedBy = config.getUpdatedBy();
    }
}