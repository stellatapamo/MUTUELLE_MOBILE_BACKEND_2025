package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assistances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assistance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // === Relation avec TypeAssistance : OneToOne bidirectionnelle ===
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_assistance_id", nullable = false)
    private TypeAssistance typeAssistance;

    // === Relation avec Member : ManyToOne ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // === Relation avec Session : ManyToOne ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

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

    @Entity
    @Table(name = "transactions_inscription")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Transactions {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "amount", precision = 12, scale = 2)
        private BigDecimal solidarityAmount = BigDecimal.ZERO;

        // === Relation avec TypeAssistance : OneToOne bidirectionnelle ===
        @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "type_assistance_id", nullable = false)
        private TypeAssistance typeAssistance;

        // === Relation avec Member : ManyToOne ===
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "member_id", nullable = false)
        private Member member;

        // === Relation avec Session : ManyToOne ===
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "session_id", nullable = false)
        private Session session;

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
}