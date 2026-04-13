package com.mutuelle.mobille.dto.sessionHistory;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SessionHistoryResponseDTO {
    private Long id;
    private Long sessionId;
    private String sessionName;
    private LocalDateTime sessionStartDate;
    private LocalDateTime sessionEndDate;
    private BigDecimal totalAssistanceAmount;
    private Long totalAssistanceCount;
    private BigDecimal agapeAmount;
    private BigDecimal mutuelleCash;
    private BigDecimal mutuellesSavingAmount;
    private BigDecimal mutuelleBorrowAmount;
    private Long totalTransactions;
    private LocalDateTime createdAt;
}
