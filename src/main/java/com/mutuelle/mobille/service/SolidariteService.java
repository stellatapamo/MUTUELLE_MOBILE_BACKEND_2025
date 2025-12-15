package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.AccountMemberRepository;
import com.mutuelle.mobille.repository.AccountMutuelleRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.repository.SolidariteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolidariteService {

        // Dépendances identiques au style de AccountService
        private final AccountMemberRepository memberRepo;
        private final AccountMutuelleRepository globalRepo;
        private final SessionRepository sessionRepo;
        private final SolidariteRepository solidariteRepo;

        /**
         * Paiement d'une cotisation de solidarité par un membre
         * - met à jour les comptes (membre + global)
         * - crée une transaction pour la traçabilité
         */
        @Transactional
        public void paySolidarity(Long memberId, BigDecimal amount, Long sessionId) {

                // ─────────────────────────────────────────────
                // VALIDATIONS MÉTIER
                // ─────────────────────────────────────────────
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Le montant de solidarité doit être positif");
                }

                AccountMember memberAccount = memberRepo.findByMemberId(memberId)
                                .orElseThrow(() -> new RuntimeException("Compte membre introuvable"));

                AccountMutuelle globalAccount = globalRepo.findAll().stream()
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Compte global introuvable"));

                Session session = sessionRepo.findById(sessionId)
                                .orElseThrow(() -> new RuntimeException("Session introuvable"));

                //
                // MISE À JOUR DES COMPTES (LOGIQUE FINANCIÈRE)
                //
                // Le membre verse une cotisation de solidarité.
                // On incrémente :
                // - le montant de solidarité du compte membre
                // - le montant de solidarité du compte global de la mutuelle
                memberAccount.setSolidarityAmount(
                                memberAccount.getSolidarityAmount().add(amount));

                globalAccount.setSolidarityAmount(
                                globalAccount.getSolidarityAmount().add(amount));

                // Persistance des nouvelles valeurs
                memberRepo.save(memberAccount);
                globalRepo.save(globalAccount);

                // ─────────────────────────────────────────────
                // 2️CRÉATION DE LA TRANSACTION (TRAÇABILITÉ)
                // ─────────────────────────────────────────────
                // La transaction permet de conserver l'historique
                // des paiements de solidarité par membre et par session.
                Transaction tx = Transaction.builder()
                                .accountMember(memberAccount)
                                .amount(amount)
                                .transactionType(TransactionType.SOLIDARITE)
                                .transactionDirection(TransactionDirection.CREDIT)
                                .session(session)
                                .build();

                // Sauvegarde de la transaction en base
                solidariteRepo.save(tx);
        }

        /**
         * Récupère l'historique des paiements de solidarité d'un membre
         */
        public List<Transaction> getSolidarityHistory(Long memberId) {

                AccountMember memberAccount = memberRepo.findByMemberId(memberId)
                                .orElseThrow(() -> new RuntimeException("Compte membre introuvable"));

                return solidariteRepo.findByTransactionTypeAndAccountMember(
                                TransactionType.SOLIDARITE,
                                memberAccount);
        }

        /**
         * Calcule le total payé en solidarité par un membre
         */
        public BigDecimal getTotalSolidarityPaid(Long memberId) {

                AccountMember memberAccount = memberRepo.findByMemberId(memberId)
                                .orElseThrow(() -> new RuntimeException("Compte membre introuvable"));

                BigDecimal total = solidariteRepo.sumAmountByTransactionTypeAndAccountMember(
                                TransactionType.SOLIDARITE,
                                memberAccount);

                return total != null ? total : BigDecimal.ZERO;
        }
}
