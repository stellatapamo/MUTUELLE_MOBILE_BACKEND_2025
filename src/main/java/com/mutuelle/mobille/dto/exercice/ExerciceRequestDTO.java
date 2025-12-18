package com.mutuelle.mobille.dto.exercice;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExerciceRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    private LocalDateTime endDate; // optionnelle (peut être null si exercice en cours)
}