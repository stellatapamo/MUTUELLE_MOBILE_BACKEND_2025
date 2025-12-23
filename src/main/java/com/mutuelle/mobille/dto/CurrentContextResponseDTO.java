package com.mutuelle.mobille.dto;

import com.mutuelle.mobille.dto.assistance.TypeAssistanceResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.models.MutuelleConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CurrentContextResponseDTO {

    // Session et exercice courant
    private SessionResponseDTO currentSession;
    private ExerciceResponseDTO currentExercice;

    // Configuration globale (optionnel)
    private MutuelleConfig config;

    private List<TypeAssistanceResponseDto> typeAssistance;

}