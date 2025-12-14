package com.mutuelle.mobille.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMemberDTO {
    private Long id;
    private BigDecimal unpaidRegistrationAmount;
    private BigDecimal solidarityAmount;
    private BigDecimal borrowAmount;
    private BigDecimal unpaidRenfoulement;
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}