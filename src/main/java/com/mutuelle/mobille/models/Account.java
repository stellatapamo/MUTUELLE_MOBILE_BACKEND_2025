package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Frais d'inscription impayés
    @Column(name = "unpaid_registration_amount", precision = 12, scale = 2)
    private BigDecimal unpaidRegistrationAmount = BigDecimal.ZERO;

    // Cotisation solidarité
    @Column(name = "solidarity_amount", precision = 12, scale = 2)
    private BigDecimal solidarityAmount = BigDecimal.ZERO;

    // Montant emprunté
    @Column(name = "borrow_amount", precision = 12, scale = 2)
    private BigDecimal borrowAmount = BigDecimal.ZERO;

    // Renflouement impayé
    @Column(name = "unpaid_renfoulement", precision = 12, scale = 2)
    private BigDecimal unpaidRenfoulement = BigDecimal.ZERO;

    @Column(name = "is_global_account", nullable = false)
    private boolean globalAccount = false; // true seulement pour le compte global

    @Column(name = "is_active")
    private boolean isActive = true;

    // Relation bidirectionnelle : un compte appartient à un seul membre (sauf le compte global)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true) // nullable = true pour le compte global
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}