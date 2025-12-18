package com.mutuelle.mobille.dto.session;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SessionResponseDTO {

    private Long id;
    private String name;
    private BigDecimal solidarityAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean inProgress;
    private Long exerciceId;
    private String exerciceName; // pour afficher le nom de l'exercice lié
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}