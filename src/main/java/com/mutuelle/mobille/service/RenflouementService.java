package com.mutuelle.mobille.service;

import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RenflouementService {

    private final TransactionRepository transactionRepository;
    private final ExerciceService exerciceService;

    public BigDecimal getMemberRenflouementContribution(Member member, Exercice exercice) {
        // Correction typo: Use RENFOULEMENT from enum as it was defined with a typo in
        // the enum file
        List<Transaction> transactions = transactionRepository
                .findByAccountMember_MemberAndTransactionTypeAndCreatedAtBetween(
                        member,
                        TransactionType.RENFOULEMENT,
                        exercice.getStartDate(),
                        exercice.getEndDate() != null ? exercice.getEndDate() : LocalDateTime.now());
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Vérifie si un membre est en règle pour le renflouement de l'exercice en
     * cours.
     * Règle : Après le mois d'avril (fin avril), le membre doit avoir payé la
     * totalité du renflouement.
     */
    public boolean isMemberCompliant(Member member) {
        Optional<Exercice> currentExerciceOpt = exerciceService.getCurrentExercice();
        if (currentExerciceOpt.isEmpty()) {
            return true; // Pas d'exercice en cours, on considère conforme par défaut ?
        }

        Exercice currentExercice = currentExerciceOpt.get();

        // 1. Vérification du renflouement spécifique
        BigDecimal targetRenflouement = currentExercice.getRenflouementAmount();
        LocalDateTime now = LocalDateTime.now();

        // Date limite : 30 Avril
        LocalDateTime deadline = LocalDateTime.of(now.getYear(), Month.APRIL, 30, 23, 59, 59);

        boolean renflouementOk = true;
        if (targetRenflouement.compareTo(BigDecimal.ZERO) > 0 && now.isAfter(deadline)) {
            BigDecimal totalRenflouementPaid = getMemberRenflouementContribution(member, currentExercice);
            renflouementOk = totalRenflouementPaid.compareTo(targetRenflouement) >= 0;
        }

        return renflouementOk;
    }
}
