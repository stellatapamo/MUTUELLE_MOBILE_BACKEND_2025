package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpruntService {

    private final AccountService accountService;
    private final InteretService interetService;
    private final SessionService sessionService;
    private final TransactionRepository transactionRepository;
    private final BorrowingCeilingService borrowingCeilingService;


    public void emprunter(Long memberId, BigDecimal montant) {

        AccountMember emprunteur = accountService.getMemberAccount(memberId);
        Optional<Session> currentSessionOpt = sessionService.getCurrentSession();

        if (currentSessionOpt.isEmpty()) {
            throw new IllegalStateException("Impossible d'effectuer un emprunt : aucune session active en cours");
        }

        Session currentSession = currentSessionOpt.get();

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

        BigDecimal plafond = borrowingCeilingService.calculerPlafond(saving);

        if (montant.compareTo(plafond) > 0) {
            throw new IllegalArgumentException(
                    "Montant refusé : plafond autorisé = " + plafond
            );
        }

        BigDecimal interet = interetService.calculerInteret(montant); // valeur de l'interet

        accountService.borrowMoney(memberId, montant);

        Transaction trans = transactionRepository.save(
                Transaction.builder()
                        .accountMember(emprunteur)
                        .amount(montant.subtract(interet))
                        .transactionType(TransactionType.EMPRUNT)
                        .transactionDirection(TransactionDirection.DEBIT)
                        .session(currentSession)
                        .build()
        );

        interetService.redistribuerInteret(emprunteur.getId(), interet,trans,currentSession);
    }

    @Transactional
    public void rembourser(Long memberId, BigDecimal montant) {

        AccountMember membre = accountService.getMemberAccount(memberId);
        Optional<Session> currentSessionOpt = sessionService.getCurrentSession();
        if (currentSessionOpt.isEmpty()) {
            throw new IllegalStateException("Impossible d'effectuer un remboursement : aucune session active en cours");
        }
        Session currentSession = currentSessionOpt.get();

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
                        .session(currentSession)
                        .build()
        );
    }
}
