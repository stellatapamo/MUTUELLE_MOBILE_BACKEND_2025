package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.mapper.TransactionMapper;
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
    private final TransactionMapper transactionMapper;

    /**
     * LISTE DES TRANSACTIONS AVEC FILTRES
     */
    public Page<TransactionResponseDTO> getTransactionsFiltered(
            TransactionType type,
            TransactionDirection direction,
            Long sessionId,
            Long accountMemberId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Page<Transaction> page = transactionRepository.findFiltered(
                type, direction, sessionId, accountMemberId, fromDate, toDate, pageable
        );

        return page.map(transactionMapper::toResponseDTO);
    }

    /**
     * DÉTAIL D’UNE TRANSACTION
     */
    public TransactionResponseDTO getTransactionById(Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction introuvable"));

        return new TransactionResponseDTO(
                tx.getId(),
                tx.getAmount(),
                tx.getTransactionType(),
                tx.getTransactionDirection(),
                tx.getAccountMember().getId(),
                tx.getAccountMember().getMember() != null
                        ? tx.getAccountMember().getMember().getFirstname() + " " +
                        tx.getAccountMember().getMember().getLastname()
                        : null,
                tx.getSession().getId(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }

    // Nombre total de transactions pour un membre
    public Long countTransactionsByMember(Long accountMemberId) {
        return transactionRepository.countByAccountMemberId(accountMemberId);
    }

    // Nombre total de transactions global
    public Long countAllTransactions() {
        return transactionRepository.count();
    }
}
