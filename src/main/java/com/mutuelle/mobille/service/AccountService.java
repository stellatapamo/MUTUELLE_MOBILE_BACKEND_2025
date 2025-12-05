package com.mutuelle.mobille.service;

import com.mutuelle.mobille.models.Account;
import com.mutuelle.mobille.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @PostConstruct
    @Transactional
    public void initGlobalAccount() {
        if (accountRepository.findByGlobalAccountTrue().isEmpty()) {
            Account global = Account.builder()
                    .globalAccount(true)
                    .isActive(true)
                    .unpaidRegistrationAmount(BigDecimal.ZERO)
                    .solidarityAmount(BigDecimal.ZERO)
                    .borrowAmount(BigDecimal.ZERO)
                    .unpaidRenfoulement(BigDecimal.ZERO)
                    .build();
            accountRepository.save(global);
        }
    }

    public Account getGlobalAccount() {
        return accountRepository.findByGlobalAccountTrue()
                .orElseThrow(() -> new RuntimeException("Compte global introuvable"));
    }
}