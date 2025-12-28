package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RenflouementService {

    private final AssistanceRepository assistanceRepository;
    private final AccountMutuelleRepository accountMutuelleRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final TransactionRepository transactionRepository;
    private final SessionRepository sessionRepository;
    private final AccountService accountService;

    @Transactional
    public void processRenflouement(
            Exercice exercice,
            BigDecimal agape
    ) {
        // Récupérer le compte global
        AccountMutuelle globalAccount = accountService.getMutuelleGlobalAccount();

        // 1️⃣ Période de l’exercice
        LocalDateTime start = exercice.getStartDate();
        LocalDateTime end = exercice.getEndDate();

        // 2️⃣ Somme des assistances
        BigDecimal totalAssistances =
                assistanceRepository.sumAssistancesBetween(start, end);

        // 3️⃣ Total renflouement
        BigDecimal totalRenflouement =
                totalAssistances.add(agape.multiply(BigDecimal.valueOf(12)));





        // 4️⃣ Membres à jour
        List<AccountMember> membresAJour =
                accountMemberRepository.findByUnpaidRegistrationAmount(BigDecimal.ZERO);

        if (membresAJour.isEmpty()) {
            throw new RuntimeException("Aucun membre à jour");
        }

        // 5️⃣ Part individuelle
        BigDecimal partIndividuelle =
                totalRenflouement.divide(
                        BigDecimal.valueOf(membresAJour.size()),
                        2,
                        RoundingMode.HALF_UP
                );





        // 6️⃣ Session de clôture
        Session session = sessionRepository.findFirstByExerciceIdOrderByCreatedAtDesc(exercice.getId())
                .orElseThrow(() -> new RuntimeException("Session de clôture introuvable"));

        // 7️⃣ Application à chaque membre
        for (AccountMember account : membresAJour) {

            // ➕ dette
            account.setUnpaidRenfoulement(
                    account.getUnpaidRenfoulement().add(partIndividuelle)
            );

            // ➕ transaction
            Transaction transaction = Transaction.builder()
                    .accountMember(account)
                    .session(session)
                    .amount(partIndividuelle)
                    .transactionType(TransactionType.RENFOULEMENT)
                    .transactionDirection(TransactionDirection.DEBIT)
                    .build();

            transactionRepository.save(transaction);
        }
        // Total renflouement non payé à la mutuelle (cumul de tous les membres)
        BigDecimal totalUnpaid = globalAccount.getUnpaidRenfoulement().add(totalRenflouement);
        globalAccount.setUnpaidRenfoulement(totalUnpaid );
        accountMutuelleRepository.save(globalAccount);
    }


    @Transactional
    public Transaction payRenflouement(
            Long memberId,
            Long sessionId,
            BigDecimal amount
    ) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        // 1️⃣ Paiement
        accountService.payRenflouement(memberId, amount);

        // 2️⃣ Transaction
        AccountMember account = accountService.getMemberAccount(memberId);

        Transaction transaction = Transaction.builder()
                .accountMember(account)
                .session(session)
                .amount(amount)
                .transactionType(TransactionType.RENFOULEMENT)
                .transactionDirection(TransactionDirection.CREDIT)
                .build();

        return transactionRepository.save(transaction);
    }
}
