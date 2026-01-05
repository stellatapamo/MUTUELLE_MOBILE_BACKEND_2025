package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
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

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final MutuelleConfigService mutuelleConfigService;

    /**
     * ============================
     * CALCUL DE L’INTÉRÊT
     * ============================
     */
    public BigDecimal calculerInteret(BigDecimal montantEmprunte) {
        MutuelleConfig  config = mutuelleConfigService.getCurrentConfig();
        return montantEmprunte.multiply(config.getLoanInterestRatePercent().divide(BigDecimal.valueOf(100)));
    }

    /**
     * ==================================================
     * REDISTRIBUTION DE L’INTÉRÊT AUX MEMBRES
     * ==================================================
     */
    public void redistribuerInteret(Long emprunteurAccountId, BigDecimal interetTotalVal,
                                    Transaction parentTransaction, Session session) {

        List<AccountMember> comptes = accountService.getAllMemberAccounts();

        List<AccountMember> beneficiaires = comptes.stream()
                .filter(acc -> acc.getSavingAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        // Aucun bénéficiaire → tout va en caisse
        if (beneficiaires.isEmpty()) {
            accountService.addToMutuelleCaisse(interetTotalVal);

            transactionRepository.save(Transaction.builder()
                    .accountMember(null)
                    .amount(interetTotalVal)
                    .description("Intérêt redistribué à la caisse (aucun épargnant)")
                    .transactionType(TransactionType.INTERET)
                    .transactionDirection(TransactionDirection.CREDIT)
                    .session(session)
                    .parentTransaction(parentTransaction)
                    .build());

            return;
        }

        long nombreBeneficiaires = beneficiaires.size();
        BigDecimal interetParMembre = interetTotalVal
                .divide(BigDecimal.valueOf(nombreBeneficiaires), 2, RoundingMode.DOWN);

        BigDecimal totalDistribue = BigDecimal.ZERO;

        for (AccountMember beneficiaire : beneficiaires) {
            // Créditer le compte du membre
            beneficiaire.setSavingAmount(beneficiaire.getSavingAmount().add(interetParMembre));
            accountService.saveMemberAccount(beneficiaire);

            // Créer la transaction de redistribution
            Transaction transactionRedistribution = Transaction.builder()
                    .accountMember(beneficiaire)
                    .amount(interetParMembre)
                    .description("Redistribution d'intérêt")
                    .transactionType(TransactionType.INTERET)
                    .transactionDirection(TransactionDirection.CREDIT)
                    .session(session)
                    .parentTransaction(parentTransaction)
                    .build();

            transactionRepository.save(transactionRedistribution);

            totalDistribue = totalDistribue.add(interetParMembre);
        }

        // Reliquat (arrondi perdu) → caisse mutuelle
        BigDecimal reliquat = interetTotalVal.subtract(totalDistribue);
        if (reliquat.compareTo(BigDecimal.ZERO) > 0) {
            accountService.addToMutuelleCaisse(reliquat);

            transactionRepository.save(Transaction.builder()
                    .accountMember(null)
                    .amount(reliquat)
                    .description("Reliquat d'intérêt (arrondi)")
                    .transactionType(TransactionType.INTERET)
                    .transactionDirection(TransactionDirection.CREDIT)
                    .session(session)
                    .parentTransaction(parentTransaction)
                    .build());
        }
    }
}
