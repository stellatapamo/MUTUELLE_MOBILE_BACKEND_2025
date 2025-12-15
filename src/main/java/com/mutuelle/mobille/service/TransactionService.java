package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<TransactionResponseDTO> getTransactionsFiltered(
            TransactionType type,
            TransactionDirection direction,
            Long sessionId,
            Long accountMemberId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Page<Transaction> page = transactionRepository.findFiltered(type, direction, sessionId, accountMemberId, fromDate, toDate, pageable);

        return page.map(transaction -> new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getTransactionDirection(),
                transaction.getAccountMember().getId(),
                transaction.getAccountMember().getMember() != null
                        ? (transaction.getAccountMember().getMember().getFirstname()+" "+transaction.getAccountMember().getMember().getLastname())
                        : null,
                transaction.getSession().getId(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        ));
    }

    public TransactionResponseDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID : " + id));

        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getTransactionDirection(),
                transaction.getAccountMember().getId(),
                transaction.getAccountMember().getMember() != null
                        ? (transaction.getAccountMember().getMember().getFirstname()+" "+transaction.getAccountMember().getMember().getLastname())
                        : null,
                transaction.getSession().getId(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}