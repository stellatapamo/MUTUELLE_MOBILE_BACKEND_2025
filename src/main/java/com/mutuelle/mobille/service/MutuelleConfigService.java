package com.mutuelle.mobille.service;

import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.repository.MutuelleConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MutuelleConfigService {

    private final MutuelleConfigRepository configRepository;

    @PostConstruct
    @Transactional
    public void initDefaultConfig() {
        if (configRepository.count() == 0) {
            MutuelleConfig defaultConfig = MutuelleConfig.builder()
                    .registrationFeeAmount(new BigDecimal("25000.00"))
                    .loanInterestRatePercent(new BigDecimal("8.00"))
                    .updatedBy("system")
                    .build();
            configRepository.save(defaultConfig);
        }
    }

    @Cacheable(value = "mutuelleConfig", key = "'current'")
    public MutuelleConfig getCurrentConfig() {
        return configRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new RuntimeException("Mutuelle configuration not found"));
    }

    @Transactional
    @CacheEvict(value = "mutuelleConfig", allEntries = true)
    public MutuelleConfig updateConfig(MutuelleConfig updatedConfig, String updatedBy) {
        MutuelleConfig config = getCurrentConfig();
        config.setRegistrationFeeAmount(updatedConfig.getRegistrationFeeAmount());
        config.setLoanInterestRatePercent(updatedConfig.getLoanInterestRatePercent());
        config.setUpdatedBy(updatedBy);
        return configRepository.save(config);
    }
}