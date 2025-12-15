package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final InteretService interetService;
    private final SessionService sessionService;

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
                ? (transaction.getAccountMember().getMember().getFirstname() + " " + transaction.getAccountMember().getMember().getLastname())
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
                ? (transaction.getAccountMember().getMember().getFirstname() + " " + transaction.getAccountMember().getMember().getLastname())
                : null,
                transaction.getSession().getId(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    /**
     * ======================= EMPRUNT D'UN MEMBRE =======================
     */
    public Transaction emprunter(Long memberId, BigDecimal montantSouhaite) {

        AccountMember emprunteur = accountService.getMemberAccount(memberId);

        // 1️⃣ Vérification : le membre a déjà épargné
        if (emprunteur.getSavingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Un membre sans épargne ne peut pas emprunter");
        }

        // 2️⃣ Vérification : un seul emprunt actif
        if (emprunteur.getBorrowAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Ce membre a déjà un emprunt actif");
        }

        // 3️⃣ Calcul du plafond (via InteretService ou Config)
        BigDecimal plafond = interetService.calculerPlafond(emprunteur.getSavingAmount());

        if (montantSouhaite.compareTo(plafond) > 0) {
            throw new IllegalArgumentException(
                    "Montant supérieur au plafond autorisé : " + plafond
            );
        }

        // 4️⃣ Calcul de l’intérêt
        BigDecimal interet = interetService.calculerInteret(montantSouhaite);

        // 5️⃣ La mutuelle sort le montant BRUT
        accountService.borrowMoney(memberId, montantSouhaite);

        // 6️⃣ Redistribution immédiate de l’intérêt
        interetService.redistribuerInteret(memberId, interet);

        // 7️⃣ Historique TRANSACTION (montant NET reçu)
        Transaction transactionEmprunt = Transaction.builder()
                .amount(montantSouhaite.subtract(interet))
                .transactionDirection(TransactionDirection.DEBIT)
                .transactionType(TransactionType.EMPRUNT)
                .accountMember(emprunteur)
                .session(sessionService.getCurrentSession())
                .build();

        transactionRepository.save(transactionEmprunt);

        // 8️⃣ Historique TRANSACTION INTERET
        Transaction transactionInteret = Transaction.builder()
                .amount(interet)
                .transactionDirection(TransactionDirection.CREDIT)
                .transactionType(TransactionType.INTERET)
                .accountMember(emprunteur)
                .session(sessionService.getCurrentSession())
                .build();

        transactionRepository.save(transactionInteret);

        return transactionEmprunt;
    }

    /**
     * ======================= REMBOURSEMENT D'UN EMPRUNT
     * =======================
     */
    public Transaction rembourser(Long memberId, BigDecimal montant) {

        AccountMember membre = accountService.getMemberAccount(memberId);

        // 1️⃣ Vérifier qu'il existe un emprunt actif
        if (membre.getBorrowAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Ce membre n'a aucun emprunt actif");
        }

        // 2️⃣ Appliquer la logique financière
        accountService.repayBorrowedAmount(memberId, montant);

        // 3️⃣ Historique TRANSACTION REMBOURSEMENT
        Transaction transaction = Transaction.builder()
                .amount(montant)
                .transactionDirection(TransactionDirection.CREDIT)
                .transactionType(TransactionType.REMBOURSSEMENT)
                .accountMember(membre)
                .session(sessionService.getCurrentSession())
                .build();

        return transactionRepository.save(transaction);
    }
}
