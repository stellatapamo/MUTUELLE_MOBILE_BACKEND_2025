package com.mutuelle.mobille.service;

import com.mutuelle.mobille.models.account.AccountMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InteretService {

    private final AccountService accountService;

    /**
     * ============================
     * CALCUL DU PLAFOND D'EMPRUNT
     * ============================
     */
    public BigDecimal calculerPlafond(BigDecimal epargne) {

        if (epargne.compareTo(BigDecimal.valueOf(500_000)) <= 0) {
            return epargne.multiply(BigDecimal.valueOf(5))
                    .min(BigDecimal.valueOf(2_000_000));
        }
        if (epargne.compareTo(BigDecimal.valueOf(1_000_000)) <= 0) {
            return epargne.multiply(BigDecimal.valueOf(4));
        }
        if (epargne.compareTo(BigDecimal.valueOf(1_500_000)) <= 0) {
            return epargne.multiply(BigDecimal.valueOf(3));
        }
        if (epargne.compareTo(BigDecimal.valueOf(2_000_000)) <= 0) {
            return epargne.multiply(BigDecimal.valueOf(2))
                    .min(BigDecimal.valueOf(4_000_000));
        }
        return epargne.multiply(BigDecimal.valueOf(1.5));
    }

    /**
     * ============================
     * CALCUL DE L’INTÉRÊT (3 %)
     * ============================
     */
    public BigDecimal calculerInteret(BigDecimal montantEmprunte) {
        return montantEmprunte.multiply(BigDecimal.valueOf(0.03));
    }

    /**
     * ==================================================
     * REDISTRIBUTION DE L’INTÉRÊT AUX MEMBRES
     * ==================================================
     */
    public void redistribuerInteret(Long emprunteurId, BigDecimal interetTotal) {

        List<AccountMember> comptes = accountService.getAllMemberAccounts();

        List<AccountMember> beneficiaires = comptes.stream()
                .filter(acc -> !acc.getMember().getId().equals(emprunteurId))
                .filter(acc -> acc.getSavingAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        // Aucun bénéficiaire → tout va en caisse
        if (beneficiaires.isEmpty()) {
            accountService.addToMutuelleCaisse(interetTotal);
            return;
        }

        BigDecimal totalEpargne = beneficiaires.stream()
                .map(AccountMember::getSavingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDistribue = BigDecimal.ZERO;

        for (AccountMember acc : beneficiaires) {

            BigDecimal partTheorique = interetTotal
                    .multiply(acc.getSavingAmount())
                    .divide(totalEpargne, 10, RoundingMode.HALF_UP);

            // Arrondi au multiple de 25
            BigDecimal partArrondie = partTheorique
                    .divide(BigDecimal.valueOf(25), 0, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(25));

            if (partArrondie.compareTo(BigDecimal.ZERO) > 0) {
                acc.setSavingAmount(acc.getSavingAmount().add(partArrondie));
                accountService.saveMemberAccount(acc);
                totalDistribue = totalDistribue.add(partArrondie);
            }
        }

        // Reliquat → caisse mutuelle
        BigDecimal reliquat = interetTotal.subtract(totalDistribue);
        if (reliquat.compareTo(BigDecimal.ZERO) > 0) {
            accountService.addToMutuelleCaisse(reliquat);
        }
    }
}
