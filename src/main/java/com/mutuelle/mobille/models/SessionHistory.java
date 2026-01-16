package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SessionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private BigDecimal totalAssistanceAmount = BigDecimal.ZERO;

    private Long totalAssistanceCount ;

    private BigDecimal agapeAmount = BigDecimal.ZERO;

    private BigDecimal mutuelleCash = BigDecimal.ZERO;

    @Column(name = "mutuelle_saving_amount", precision = 12, scale = 2)
    private BigDecimal mutuellesSavingAmount = BigDecimal.ZERO;

    @Column(name = "mutuelle_borrow_amount", precision = 12, scale = 2)
    private BigDecimal mutuelleBorrowAmount = BigDecimal.ZERO;

    private Long totalTransactions ;


    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}