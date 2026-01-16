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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpruntService {

    private final AccountService accountService;
    private final InteretService interetService;
    private final SessionService sessionService;
    private final TransactionRepository transactionRepository;
    private final BorrowingCeilingService borrowingCeilingService;

    @Transactional
    public void emprunter(Long memberId, BigDecimal montant) {

        AccountMember emprunteur = accountService.getMemberAccount(memberId);
        Optional<Session> currentSessionOpt = sessionService.findCurrentSession();

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
        Optional<Session> currentSessionOpt = sessionService.findCurrentSession();
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

    @Transactional
    public void calculerEtRedistribuerInteretsTrimestriels() {

        Optional<Session> sessionOpt = sessionService.findCurrentSession();
        if (sessionOpt.isEmpty()) {
//            log.warn("Aucune session active → calcul intérêts trimestriels ignoré");
            return;
        }
        Session session = sessionOpt.get();

        List<AccountMember> emprunteurs = accountService.findMembersWithBorrowGreaterThanZero();
        if (emprunteurs.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (AccountMember membreAcc : emprunteurs) {

            LocalDateTime lastInterest = membreAcc.getLastInterestDate();
            if (lastInterest == null) {
                // À améliorer : idéalement stocker la vraie date de premier emprunt
                lastInterest = membreAcc.getCreatedAt() != null ? membreAcc.getCreatedAt() : now.minusMonths(3);
            }

            if (!aAtteintProchainTrimestre(lastInterest, now)) {
                continue;
            }

            BigDecimal soldeActuel = membreAcc.getBorrowAmount();
            if (soldeActuel.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 1. Calcul intérêts trimestriels
            BigDecimal interets = interetService.calculerInteret(soldeActuel);
            if (interets.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 2. Calcul du montant emprunt initial "équivalent"
            BigDecimal montantEquivalent = interetService.calculMontantEmpruntEquivalent(soldeActuel);

            // 3. Plafond actuel (sur l'épargne du jour)
            BigDecimal epargneActuelle = membreAcc.getSavingAmount() != null ? membreAcc.getSavingAmount() : BigDecimal.ZERO;
            BigDecimal plafondActuel = borrowingCeilingService.calculerPlafond(epargneActuelle);

            // 4. Décision
            if (montantEquivalent.compareTo(plafondActuel) > 0) {
//                // Alerte – ne pas prélever
//                log.info("Plafond dépassé pour membre {} | solde={} | equiv={} | plafond={}",
//                        membreAcc.getId(), soldeActuel, montantEquivalent, plafondActuel);
//
//                createAlertePlafondDepasse(membre, soldeActuel, montantEquivalent, plafondActuel, interets, session);
//                // Note : on n'avance PAS la date ici → on réessaiera au prochain passage
                continue;
            }

            // 5. OK → on augmente la dette
            accountService.addBorrowAmount(membreAcc, interets);

            // 6. Transaction visible pour l'emprunteur
            Transaction txInteret = transactionRepository.save(
                    Transaction.builder()
                            .accountMember(membreAcc)
                            .amount(interets)
                            .transactionType(TransactionType.INTERET)
                            .transactionDirection(TransactionDirection.DEBIT)
                            .session(session)
                            .description("Intérêts trimestriels sur solde restant")
                            .build()
            );

            // 7. Redistribution (comme à l'emprunt initial)
            interetService.redistribuerInteret(membreAcc.getId(), interets, txInteret, session);

            // 8. Avancer la date du dernier calcul
            LocalDateTime nextDate = lastInterest.plusMonths(3);
            accountService.updateLastInterestDate(membreAcc.getId(), nextDate);

//            log.info("Intérêts trimestriels appliqués membre {} | solde={} → {} | intérêts={} | equiv={}",
//                    membreAcc.getId(), soldeActuel, soldeActuel.add(interets), interets, montantEquivalent);
        }
    }

    private boolean aAtteintProchainTrimestre(LocalDateTime derniere, LocalDateTime actuelle) {
        if (derniere == null) return true;

        LocalDateTime prochain = derniere.plusMonths(3);
        return !actuelle.isBefore(prochain);
    }

    private boolean estNouveauTrimestre(LocalDateTime derniere, LocalDateTime maintenant) {
        int trimestreActuel = getTrimestre(maintenant);
        int trimestreDernier = getTrimestre(derniere);

        return trimestreActuel != trimestreDernier;
    }

    private int getTrimestre(LocalDateTime date) {
        int mois = date.getMonthValue();
        return (mois - 1) / 3 + 1; // 1→3 → T1, 4→6 → T2, etc.
    }
}
