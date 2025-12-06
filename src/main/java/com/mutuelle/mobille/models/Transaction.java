package com.mutuelle.mobille.models;

import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.account.AccountMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "transaction_direction", nullable = false)
    private TransactionDirection transactionDirection;
    
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    // === Relation avec accounts_member : ManyToOne ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_member_id", nullable = false)
    private AccountMember accountMember;

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