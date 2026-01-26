package com.mutuelle.mobille.dto.exercice;

import com.mutuelle.mobille.enums.StatusExercice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExerciceHistoryDto {

    private Long id;

    // Informations sur l'exercice concerné
    private Long exerciceId;
    private String exerciceName;
    private LocalDateTime exerciceStartDate;
    private LocalDateTime exerciceEndDate;
    private StatusExercice exerciceStatus;    

    // Statistiques et montants issus de l'exercice
    private BigDecimal totalAssistanceAmount;
    private Long totalAssistanceCount;

    private BigDecimal totalAgapeAmount;

    private BigDecimal mutuelleCash;           // solde de solidarité mutuelle
    private BigDecimal mutuellesSavingAmount;  // épargne mutuelle
    private BigDecimal mutuelleBorrowAmount;   // emprunt mutuelle

    private Long totalTransactions;

    // Métadonnées de création
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}