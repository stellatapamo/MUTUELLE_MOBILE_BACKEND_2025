package com.mutuelle.mobille.mapper.transactionEpargne;

import com.mutuelle.mobille.dto.*;
import com.mutuelle.mobille.dto.member.AccountMemberDTO;
import com.mutuelle.mobille.dto.member.MemberProfileDTO;
import com.mutuelle.mobille.dto.transaction.epargne.TransactionEpargneDto;
import com.mutuelle.mobille.models.*;
import com.mutuelle.mobille.models.account.AccountMember;

import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

    public static MemberProfileDTO toMemberDto(Member member) {
        if (member == null) return null;

        return MemberProfileDTO.builder()
                .id(member.getId())
                .firstname(member.getFirstname())
                .lastname(member.getLastname())
                .phone(member.getPhone())
                .account(toAccountMemberDto(member.getAccountMember()))
                .build();
    }


    public static AccountMemberDTO toAccountMemberDto(AccountMember account) {
        if (account == null) return null;

        return AccountMemberDTO.builder()
                .id(account.getId())
                .savingAmount(account.getSavingAmount())
                .unpaidRegistrationAmount(account.getUnpaidRegistrationAmount())
                .solidarityAmount(account.getSolidarityAmount())
                .borrowAmount(account.getBorrowAmount())
                .unpaidRenfoulement(account.getUnpaidRenfoulement())
                .isActive(account.isActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }


    public static TransactionEpargneDto toTransactionDto(Transaction transaction) {
        if (transaction == null) return null;

        return TransactionEpargneDto.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .transactionDirection(transaction.getTransactionDirection())
                .transactionType(transaction.getTransactionType().name())
                .member(toMemberDto(transaction.getAccountMember().getMember()))
                .build();
    }

    // liste de transactions
    public static List<TransactionEpargneDto> toTransactionDtoList(List<Transaction> transactions) {
        return transactions.stream()
                .map(DtoMapper::toTransactionDto)
                .collect(Collectors.toList());
    }

}

