package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "renfoulements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renfoulement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice; 

    @Column(name = "total_to_distribute_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalToDistributeAmount = BigDecimal.ZERO;

    @Column(name = "base_members_count", nullable = false)
    private int baseMembersCount = 0;

    @Column(name = "unit_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitAmount = BigDecimal.ZERO;

    @Column(name = "distributed_members_count", nullable = false)
    private int distributedMembersCount = 0;

    @Column(name = "expected_total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal expectedTotalAmount = BigDecimal.ZERO;

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