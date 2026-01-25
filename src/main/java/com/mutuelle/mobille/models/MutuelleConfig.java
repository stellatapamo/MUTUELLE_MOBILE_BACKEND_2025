package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mutuelle_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MutuelleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Registration / inscription fee
    @Column(name = "registration_fee_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal registrationFeeAmount = new BigDecimal("25000.00");

    // Loan interest rate (annual percentage)
    @Column(name = "loan_interest_rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal loanInterestRatePercent = new BigDecimal("3.00");

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}