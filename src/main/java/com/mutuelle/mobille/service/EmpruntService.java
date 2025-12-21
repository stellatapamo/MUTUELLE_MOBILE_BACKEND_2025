package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmpruntService {

    private final AccountService accountService;
    private final InteretService interetService;
    private final SessionService sessionService;
    private final TransactionRepository transactionRepository;

    public void emprunter(Long memberId, BigDecimal montant) {

        AccountMember emprunteur = accountService.getMemberAccount(memberId);

        BigDecimal saving = emprunteur.getSavingAmount() == null
                ? BigDecimal.ZERO
                : emprunteur.getSavingAmount();

        if (saving.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Impossible d’effectuer un emprunt : aucune épargne enregistrée"
            );
        }

        BigDecimal borrow = emprunteur.getBorrowAmount() == null
                ? BigDecimal.ZERO
                : emprunteur.getBorrowAmount();

        if (borrow.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Impossible d’effectuer un emprunt : un emprunt est déjà en cours"
            );
        }

        BigDecimal plafond = interetService.calculerPlafond(saving);

        if (montant.compareTo(plafond) > 0) {
            throw new IllegalArgumentException(
                    "Montant refusé : plafond autorisé = " + plafond
            );
        }

        BigDecimal interet = interetService.calculerInteret(montant);

        accountService.borrowMoney(memberId, montant);
        interetService.redistribuerInteret(memberId, interet);

        transactionRepository.save(
                Transaction.builder()
                        .accountMember(emprunteur)
                        .amount(montant.subtract(interet))
                        .transactionType(TransactionType.EMPRUNT)
                        .transactionDirection(TransactionDirection.DEBIT)
                        .session(sessionService.getCurrentSession())
                        .build()
        );

        transactionRepository.save(
                Transaction.builder()
                        .accountMember(emprunteur)
                        .amount(interet)
                        .transactionType(TransactionType.INTERET)
                        .transactionDirection(TransactionDirection.CREDIT)
                        .session(sessionService.getCurrentSession())
                        .build()
        );
    }

    @Transactional
    public void rembourser(Long memberId, BigDecimal montant) {

        AccountMember membre = accountService.getMemberAccount(memberId);

        BigDecimal borrow = membre.getBorrowAmount() == null
                ? BigDecimal.ZERO
                : membre.getBorrowAmount();

        if (borrow.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Aucun emprunt actif à rembourser"
            );
        }

        accountService.repayBorrowedAmount(memberId, montant);

        transactionRepository.save(
                Transaction.builder()
                        .accountMember(membre)
                        .amount(montant)
                        .transactionType(TransactionType.REMBOURSSEMENT)
                        .transactionDirection(TransactionDirection.CREDIT)
                        .session(sessionService.getCurrentSession())
                        .build()
        );
    }
}
