package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.*;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.repository.TransactionRepository;
import com.mutuelle.mobille.util.MoneyUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenfoulementService {

    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Calcule et assigne le renfoulement pour tous les membres actifs à la fin d'un exercice.
     * Le montant unitaire est calculé sur la base des membres à jour uniquement.
     * La dette est ensuite appliquée à tous les membres actifs.
     *
     * @param exercice l'exercice qui vient de se terminer
     */
    @Transactional
    public void calculateAndAssignRenfoulementForExercice(Exercice exercice) {

        ExerciceHistory history = exercice.getHistory();
        if (history == null) {
            log.warn("Impossible de calculer le renfoulement : aucun historique pour l'exercice {}", exercice.getId());
            return;
        }

        // 1. Calcul du total à répartir
        BigDecimal totalAssistances = history.getTotalAssistanceAmount() != null
                ? history.getTotalAssistanceAmount()
                : BigDecimal.ZERO;

        BigDecimal totalAgapes = history.getTotalAgapeAmount() != null
                ? history.getTotalAgapeAmount()
                : BigDecimal.ZERO;

        BigDecimal totalADistribuer = totalAssistances.add(totalAgapes);

        if (totalADistribuer.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Aucun renfoulement à calculer pour l'exercice {} (total = 0)", exercice.getId());
            return;
        }

        // 2. Membres à jour (ceux qui servent de base au calcul du montant unitaire)
        List<AccountMember> comptesAJour = memberRepository.findAllActiveWithAccount().stream()
                .map(Member::getAccountMember)
                .filter(compte -> compte != null
                        && compte.getUnpaidRegistrationAmount().compareTo(BigDecimal.ZERO) == 0
                        && compte.getUnpaidSolidarityAmount().compareTo(BigDecimal.ZERO) == 0)
                .toList();

        int nbMembresAJour = comptesAJour.size();
        if (nbMembresAJour == 0) {
            log.warn("Aucun membre à jour → impossible de calculer le renfoulement pour l'exercice {}", exercice.getId());
            return;
        }

        // 3. Montant unitaire de renfoulement (arrondi commercial vers le bas à la tranche de 25)
        BigDecimal renfoulementUnitaire = MoneyUtil.floorToNearest25(
                totalADistribuer.divide(
                        BigDecimal.valueOf(nbMembresAJour),
                        2,
                        RoundingMode.HALF_UP
                )
        );

        if (renfoulementUnitaire.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 4. Tous les membres actifs (ceux qui vont recevoir la dette)
        List<Member> membresActifs = memberRepository.findByIsActiveTrue();

        if (membresActifs.isEmpty()) {
            log.info("Aucun membre actif pour assigner le renfoulement");
            return;
        }

        // Récupérer une session de référence (la plus récente par ex.)
        Set<Session> sessions = exercice.getSessions();
        if (sessions.isEmpty()) {
            log.error("Impossible de créer renfoulement : aucune session pour l'exercice {}", exercice.getId());
            return;
        }

        // Session la plus récente
        Session sessionRef = sessions.stream()
                .max(Comparator.comparing(Session::getStartDate))
                .orElse(null);


        // 5. Création des transactions et mise à jour des comptes
        for (Member member : membresActifs) {
            AccountMember compte = member.getAccountMember();
            if (compte == null) {
                continue;
            }

            // Mise à jour de la dette de renfoulement
            BigDecimal detteActuelle = compte.getUnpaidRenfoulement() != null
                    ? compte.getUnpaidRenfoulement()
                    : BigDecimal.ZERO;

            BigDecimal nouvelleDette = detteActuelle.add(renfoulementUnitaire);
            compte.setUnpaidRenfoulement(nouvelleDette);

            // Création de la transaction
            Transaction transaction = Transaction.builder()
                    .amount(renfoulementUnitaire)
                    .transactionType(TransactionType.RENFOULEMENT)
                    .transactionDirection(TransactionDirection.DEBIT)
                    .description(String.format("Renfoulement exercice %s",
                            exercice.getName() != null ? exercice.getName() : exercice.getId()))
                    .accountMember(compte)
                    .session(sessionRef)
                    .build();

            transactionRepository.save(transaction);
        }

        // 6. Sauvegarde des modifications sur les AccountMember (si cascade n'est pas configuré)
        // Note : si Member a CascadeType.ALL sur accountMember, ce n'est pas nécessaire
        // memberRepository.saveAll(membresActifs);

        log.info("Renfoulement calculé pour exercice {} : {} Fcfa par membre (base : {} membres à jour), appliqué à {} membres actifs",
                exercice.getId(), renfoulementUnitaire, nbMembresAJour, membresActifs.size());
    }
}