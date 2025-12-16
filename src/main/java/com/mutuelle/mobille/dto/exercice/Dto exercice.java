package mutuelle.back.exercise;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class CreerExerciceRequest {
    @NotBlank(message = "Le nom de l'exercice est obligatoire")
    private String nom;
    
    @NotNull(message = "La date de début est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;
}

@Data
public class ExerciceDTO {
    private Long id;
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private Double epargneTotale;
    private Double solidariteTotale;
    private Double renflouementDu;
}