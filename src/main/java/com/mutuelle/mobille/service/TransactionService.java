package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
import com.mutuelle.mobille.models.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final SessionService sessionService;

    /**
     * Effectuer un emprunt
     */
    public Transaction emprunter(Long memberId, BigDecimal montantSouhaite) {

        AccountMember account = accountService.getMemberAccount(memberId);

        // 1️⃣ Vérifier que le membre a déjà épargné
        BigDecimal epargne = account.getSavingAmount();
        if (epargne.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Un membre sans épargne ne peut pas emprunter");
        }

        // 2️⃣ Vérifier qu'il n'a pas déjà un emprunt actif
        if (account.getBorrowAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Un membre ne peut avoir qu'un seul emprunt actif");
        }

        // 3️⃣ Calculer le plafond autorisé
        BigDecimal plafond = calculerPlafond(epargne);

        if (montantSouhaite.compareTo(plafond) > 0) {
            throw new IllegalArgumentException(
                    "Montant demandé supérieur au plafond autorisé : " + plafond
            );
        }

        // 4️⃣ Calculer l’intérêt (3%)
        BigDecimal tauxInteret = BigDecimal.valueOf(0.03);
        BigDecimal interet = montantSouhaite.multiply(tauxInteret);

        // Montant réellement versé au membre
        BigDecimal montantNet = montantSouhaite.subtract(interet);

        // 5️⃣ Appliquer la logique financière (débit)
        accountService.borrowMoney(memberId, montantSouhaite);

        // 6️⃣ Créer la transaction EMPRUNT
        Transaction transaction = Transaction.builder()
                .amount(montantNet)
                .transactionDirection(TransactionDirection.DEBIT)
                .transactionType(TransactionType.EMPRUNT)
                .accountMember(account)
                .session(sessionService.getCurrentSession())
                .build();

        return transactionRepository.save(transaction);
    }

    /**
     * Calcul du plafond selon l'épargne
     */
    public BigDecimal calculerPlafond(BigDecimal epargne) {

        BigDecimal plafond;

        if (epargne.compareTo(BigDecimal.valueOf(500_000)) <= 0) {
            plafond = epargne.multiply(BigDecimal.valueOf(5));
            plafond = plafond.min(BigDecimal.valueOf(2_000_000));
        }
        else if (epargne.compareTo(BigDecimal.valueOf(1_000_000)) <= 0) {
            plafond = epargne.multiply(BigDecimal.valueOf(4));
        }
        else if (epargne.compareTo(BigDecimal.valueOf(1_500_000)) <= 0) {
            plafond = epargne.multiply(BigDecimal.valueOf(3));
        }
        else if (epargne.compareTo(BigDecimal.valueOf(2_000_000)) <= 0) {
            plafond = epargne.multiply(BigDecimal.valueOf(2));
            plafond = plafond.min(BigDecimal.valueOf(4_000_000));
        }
        else {
            plafond = epargne.multiply(BigDecimal.valueOf(1.5));
        }

        return plafond;
    }

    /**
     * Obtenir le montant max empruntable
     */
    public BigDecimal getPlafondEmprunt(Long memberId) {
        AccountMember account = accountService.getMemberAccount(memberId);
        return calculerPlafond(account.getSavingAmount());
    }
}

