package com.mutuelle.mobille.dto.transaction.epargne;

import com.mutuelle.mobille.dto.profile.MemberProfileDTO;
import com.mutuelle.mobille.enums.TransactionDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEpargneDto {

    private Long id;
    private BigDecimal amount;
    private TransactionDirection transactionDirection;
    private String transactionType;

    private MemberProfileDTO member;
}

