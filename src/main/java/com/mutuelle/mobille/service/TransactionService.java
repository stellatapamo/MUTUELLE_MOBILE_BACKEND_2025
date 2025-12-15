package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final AccountService accountService;
    private final InteretService interetService;   // ✅ NOUVEAU
    private final TransactionRepository transactionRepository;
    private final SessionService sessionService;

    /**
     * =======================
     * EMPRUNT D'UN MEMBRE
     * =======================
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
     * =======================
     * REMBOURSEMENT D'UN EMPRUNT
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
