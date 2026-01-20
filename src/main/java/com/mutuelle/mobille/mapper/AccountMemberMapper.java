package com.mutuelle.mobille.mapper;

import com.mutuelle.mobille.dto.member.AccountMemberDTO;
import com.mutuelle.mobille.models.account.AccountMember;

public class AccountMemberMapper {

    public static AccountMemberDTO toAccountMemberDto(AccountMember account) {
        if (account == null) return null;

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
                .build();
    }
}

