    package com.mutuelle.mobille.service;

    import com.mutuelle.mobille.dto.account.AccountMemberResponseDTO;
    import com.mutuelle.mobille.dto.account.AccountMutuelleResponseDTO;
    import com.mutuelle.mobille.models.Member;
    import com.mutuelle.mobille.models.account.AccountMember;
    import com.mutuelle.mobille.models.account.AccountMutuelle;
    import com.mutuelle.mobille.repository.AccountMemberRepository;
    import com.mutuelle.mobille.repository.AccountMutuelleRepository;
    import jakarta.annotation.PostConstruct;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import java.util.List;

    import java.math.BigDecimal;

    @Service
    @RequiredArgsConstructor
    public class AccountService {

        private final AccountMutuelleRepository globalRepo;      // Compte global
        private final AccountMemberRepository memberRepo;        // Comptes membres

        @PostConstruct
        @Transactional
        public void initGlobalAccount() {
            if (globalRepo.count() == 0) {
                AccountMutuelle global = AccountMutuelle.builder()
                        .savingAmount(BigDecimal.ZERO)
                        .solidarityAmount(BigDecimal.ZERO)
                        .borrowAmount(BigDecimal.ZERO)
                        .totalRegistrationAmount(BigDecimal.ZERO)
                        .totalRenfoulement(BigDecimal.ZERO)
                        .isActive(true)
                        .build();
                globalRepo.save(global);
            }
        }

        // Récupérer le compte global (unique)
        public AccountMutuelle getMutuelleGlobalAccount() {
            return globalRepo.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Compte global mutuelle introuvable"));
        }

        // Récupérer le compte d'un membre
        public AccountMember getMemberAccount(Long memberId) {
            return memberRepo.findByMemberId(memberId)
                    .orElseThrow(() -> new RuntimeException("Compte membre introuvable pour l'ID : " + memberId));
        }


        // ──────────────────────────────────────────────────────────────
        // ─────────────── OPÉRATIONS FINANCIÈRES (avec mise à jour globale)
        // ──────────────────────────────────────────────────────────────

        /**
         * Un membre fait une épargne
         */
        @Transactional
        public void addSaving(Long memberId, BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant d'épargne doit être positif");
            }

            AccountMember memberAccount = getMemberAccount(memberId);
            AccountMutuelle globalAccount = getMutuelleGlobalAccount();

            BigDecimal currentAmount = memberAccount.getSavingAmount();
            if (currentAmount == null) {
                currentAmount = BigDecimal.ZERO;
            }

            // Mise à jour compte membre
            memberAccount.setSavingAmount(currentAmount.add(amount));

            // Mise à jour compte global (la mutuelle reçoit aussi cette épargne)
            globalAccount.setSavingAmount(currentAmount.add(amount));

            memberRepo.save(memberAccount);
            globalRepo.save(globalAccount);
        }

        /**
         * Retrait d'épargne par un membre
         */
        @Transactional
        public void withdrawSaving(Long memberId, BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant du retrait doit être positif");
            }

            AccountMember memberAccount = getMemberAccount(memberId);
            AccountMutuelle globalAccount = getMutuelleGlobalAccount();

            BigDecimal currentAmount = memberAccount.getSavingAmount();
            if (currentAmount == null) {
                currentAmount = BigDecimal.ZERO;
            }


            // Vérifie que le membre a assez d'épargne
            if (currentAmount.compareTo(amount) < 0) {
                throw new IllegalStateException("Solde insuffisant pour effectuer le retrait");
            }

            // Mise à jour du compte membre
            memberAccount.setSavingAmount(currentAmount.subtract(amount));

            // Mise à jour du compte global
            globalAccount.setSavingAmount(globalAccount.getSavingAmount().subtract(amount));

            // Sauvegarde
            memberRepo.save(memberAccount);
            globalRepo.save(globalAccount);
        }

        /**
         * Un membre paie les frais d'inscription
         */
        @Transactional
        public void payRegistrationFee(Long memberId, BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant des frais doit être positif");
            }

            AccountMember memberAccount = getMemberAccount(memberId);
            AccountMutuelle globalAccount = getMutuelleGlobalAccount();

            BigDecimal currentUnpaid = memberAccount.getUnpaidRegistrationAmount();

            if (currentUnpaid.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalStateException("Aucuns frais d'inscription impayés");
            }

            if (amount.compareTo(currentUnpaid) > 0) {
                throw new IllegalArgumentException("Le paiement dépasse le montant dû");
            }

            // Mise à jour compte membre
            memberAccount.setUnpaidRegistrationAmount(currentUnpaid.subtract(amount));

            // La mutuelle reçoit l'argent payé → augmente son épargne globale
            globalAccount.setSavingAmount(globalAccount.getSavingAmount().add(amount));

            memberRepo.save(memberAccount);
            globalRepo.save(globalAccount);
        }

        /**
         * Retourne tous les comptes membres
         */
        public List<AccountMember> getAllMemberAccounts() {
            return memberRepo.findAll();
        }

        public List<AccountMember> getAllMemberAccountsWithActive(boolean isActive) {
            return memberRepo.findAllByIsActive(isActive);
        }

        /**
         * Sauvegarde un compte membre (après redistribution)
         */
        public void saveMemberAccount(AccountMember account) {
            memberRepo.save(account);
        }

        /**
         * Ajoute un montant à la caisse de la mutuelle
         */
        @Transactional
        public void addToMutuelleCaisse(BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            AccountMutuelle global = getMutuelleGlobalAccount();
            global.setSavingAmount(global.getSavingAmount().add(amount));
            globalRepo.save(global);
        }

        /**
         * retirer un montant à la caisse de la mutuelle
         */
        @Transactional
        public void removeToMutuelleCaisse(BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            AccountMutuelle global = getMutuelleGlobalAccount();
            global.setSavingAmount(global.getSavingAmount().subtract(amount));
            globalRepo.save(global);
        }

        /**
         * Un membre emprunte de l'argent à la mutuelle
         */
        @Transactional
        public void borrowMoney(Long memberId, BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant emprunté doit être positif");
            }

            AccountMember memberAccount = getMemberAccount(memberId);
            AccountMutuelle globalAccount = getMutuelleGlobalAccount();

            // Vérifie que la mutuelle a assez d'épargne globale
            if (globalAccount.getSavingAmount().compareTo(amount) < 0) {
                throw new IllegalStateException("Fonds insuffisants dans la mutuelle");
            }

            memberAccount.setBorrowAmount(memberAccount.getBorrowAmount().add(amount));
            globalAccount.setSavingAmount(globalAccount.getSavingAmount().subtract(amount)); // prêt sorti

            memberRepo.save(memberAccount);
            globalRepo.save(globalAccount);
        }

        /**
         * Remboursement d'un emprunt par un membre
         */
        @Transactional
        public void repayBorrowedAmount(Long memberId, BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant remboursé doit être positif");
            }

            AccountMember memberAccount = getMemberAccount(memberId);
            AccountMutuelle globalAccount = getMutuelleGlobalAccount();

            if (memberAccount.getBorrowAmount().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Le remboursement dépasse l'emprunt en cours");
            }

            memberAccount.setBorrowAmount(memberAccount.getBorrowAmount().subtract(amount));
            globalAccount.setSavingAmount(globalAccount.getSavingAmount().add(amount));

            memberRepo.save(memberAccount);
            globalRepo.save(globalAccount);
        }


        /**
         * Paiement des frais d'inscription par un membre (partiel ou total)
         */
        @Transactional
        public void payFeeInscriptionAmount(Long memberId, BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant payé doit être positif");
            }

            AccountMember memberAccount = getMemberAccount(memberId);
            AccountMutuelle globalAccount = getMutuelleGlobalAccount();

            BigDecimal unpaid = memberAccount.getUnpaidRegistrationAmount();

            if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Aucuns frais d'inscription impayés");
            }

            if (amount.compareTo(unpaid) > 0) {
                throw new IllegalArgumentException("Le paiement dépasse les frais d'inscription dus");
            }

            // Réduire la dette du membre
            memberAccount.setUnpaidRegistrationAmount(unpaid.subtract(amount));

            // L'argent entre dans la caisse de la mutuelle
            globalAccount.setTotalRegistrationAmount(globalAccount.getTotalRegistrationAmount().add(amount));

            memberRepo.save(memberAccount);
            globalRepo.save(globalAccount);
        }

        /**
         * Paiement du renfoulement par un membre (partiel ou total)
         */
        @Transactional
        public void payRenfoulementAmount(Long memberId, BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant payé doit être positif");
            }

            AccountMember memberAccount = getMemberAccount(memberId);
            AccountMutuelle globalAccount = getMutuelleGlobalAccount();

            BigDecimal unpaid = memberAccount.getUnpaidRenfoulement();

            if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Aucun renfoulement impayé");
            }

            if (amount.compareTo(unpaid) > 0) {
                throw new IllegalArgumentException("Le paiement dépasse le renfoulement dû");
            }

            // Réduire la dette du membre
            memberAccount.setUnpaidRenfoulement(unpaid.subtract(amount));

            // L'argent entre dans la caisse de la mutuelle
            globalAccount.setTotalRenfoulement(globalAccount.getTotalRenfoulement().add(amount));

            memberRepo.save(memberAccount);
            globalRepo.save(globalAccount);
        }

        /**
         * Récupère un compte membre par son ID (ID de la table accounts_member)
         */
        public AccountMember getMemberAccountById(Long accountId) {
            return memberRepo.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Compte membre introuvable avec l'ID : " + accountId));
        }

        /**
         * Récupère le compte d'un membre à partir de l'ID du membre
         */
        public AccountMember getMemberAccountByMemberId(Long memberId) {
            return memberRepo.findByMemberId(memberId)
                    .orElseThrow(() -> new RuntimeException("Compte membre introuvable pour le membre ID : " + memberId));
        }
    }