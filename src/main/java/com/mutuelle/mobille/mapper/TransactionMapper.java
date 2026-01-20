package com.mutuelle.mobille.mapper;


import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import com.mutuelle.mobille.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDTO toResponseDTO(Transaction tx) {
        if (tx == null) {
            return null;
        }

        String memberFullName = null;
        if (tx.getAccountMember() != null && tx.getAccountMember().getMember() != null) {
            memberFullName = tx.getAccountMember().getMember().getFirstname() + " " +
                    tx.getAccountMember().getMember().getLastname();
        }

        return new TransactionResponseDTO(
                tx.getId(),
                tx.getAmount(),
                tx.getTransactionType(),
                tx.getTransactionDirection(),
                tx.getAccountMember() != null ? tx.getAccountMember().getId() : null,
                memberFullName,
                tx.getSession() != null ? tx.getSession().getId() : null,
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }
}