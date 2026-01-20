package com.mutuelle.mobille.mapper;

import com.mutuelle.mobille.dto.member.AccountMemberDTO;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.service.BorrowingCeilingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountMemberMapper {

    private final BorrowingCeilingService borrowingCeilingService;

    public AccountMemberDTO toAccountMemberDto(AccountMember account) {
        if (account == null) return null;
        BigDecimal plafond=borrowingCeilingService.calculerPlafond(account.getSavingAmount());

        return AccountMemberDTO.builder()
                .id(account.getId())
                .savingAmount(account.getSavingAmount())
                .unpaidRegistrationAmount(account.getUnpaidRegistrationAmount())
                .solidarityAmount(account.getSolidarityAmount())
                .unpaidSolidarityAmount(account.getUnpaidSolidarityAmount())
                .memberId(account.getMember().getId())
                .borrowAmount(account.getBorrowAmount())
                .lastInterestDate(account.getLastInterestDate())
                .unpaidRenfoulement(account.getUnpaidRenfoulement())
                .isActive(account.isActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .maxBorrow(plafond)
                .build();
    }
}

