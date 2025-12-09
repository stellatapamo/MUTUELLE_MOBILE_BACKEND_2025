
package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.SolidariteRepository;
import com.mutuelle.mobille.repository.AccountMemberRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class SolidariteService {

    private final SolidariteRepository solidariteRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final SessionRepository sessionRepository;

    // Constructeur pour l'injection de dépendances
    public SolidariteService(
            SolidariteRepository solidariteRepository,
            AccountMemberRepository accountMemberRepository,
            SessionRepository sessionRepository) {
        this.solidariteRepository = solidariteRepository;
        this.accountMemberRepository = accountMemberRepository;
        this.sessionRepository = sessionRepository;
    }

    // Méthode pour enregistrer une transaction de solidarité

    /**
     * Enregistre un paiement de solidarité
     * 
     * @param membreId  ID du compte membre qui paie
     * @param montant   Montant payé
     * @param sessionId ID de la session concernée
     * @return La transaction créée
     * @throws RuntimeException si le membre ou la session n'existe pas
     */
    public Transaction enregistrerPaiement(Long membreId, BigDecimal montant, Long sessionId) {

        // 1. Vérifier que le montant est valide
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à 0");
        }

        // 2. Récupérer le membre (ou lever une exception si n'existe pas)
        AccountMember accountMember = accountMemberRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre avec ID " + membreId + " introuvable"));

        // 3. Récupérer la session (ou lever une exception si n'existe pas)
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session avec ID " + sessionId + " introuvable"));

        // 4. Créer la transaction de solidarité
        Transaction transaction = Transaction.builder()
                .amount(montant)
                .transactionType(TransactionType.SOLIDARITE)
                .transactionDirection(TransactionDirection.CREDIT)
                .accountMember(accountMember)
                .session(session)
                .build();

        // 5. Sauvegarder et retourner la transaction
        return solidariteRepository.save(transaction);
    }

    /**
     * Récupère l'historique des paiements de solidarité d'un membre
     * 
     * @param membreId ID du compte membre
     * @return Liste de toutes les transactions de solidarité du membre
     * @throws RuntimeException si le membre n'existe pas
     */
    public List<Transaction> getHistoriquePaiements(Long membreId) {

        // 1. Vérifier que le membre existe
        AccountMember accountMember = accountMemberRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre avec ID " + membreId + " introuvable"));

        // 2. Récupérer toutes les transactions de solidarité du membre
        return solidariteRepository.findByTransactionTypeAndAccountMember(
                TransactionType.SOLIDARITE,
                accountMember);
    }
    // calcule le total des paiements de solidarité effectués par un membre

    /**
     * Calcule le total payé en solidarité par un membre
     * 
     * @param membreId ID du compte membre
     * @return Le montant total payé (0 si aucun paiement)
     * @throws RuntimeException si le membre n'existe pas
     */
    public BigDecimal calculerTotalPaye(Long membreId) {

        // 1. Vérifier que le membre existe
        AccountMember accountMember = accountMemberRepository.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre avec ID " + membreId + " introuvable"));

        // 2. Calculer la somme (peut retourner null si aucun paiement)
        BigDecimal total = solidariteRepository.sumAmountByTransactionTypeAndAccountMember(
                TransactionType.SOLIDARITE,
                accountMember);

        // 3. Retourner 0 si null, sinon le total
        return total != null ? total : BigDecimal.ZERO;
    }
}