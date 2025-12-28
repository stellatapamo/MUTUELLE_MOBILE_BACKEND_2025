package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.CurrentContextResponseDTO;
import com.mutuelle.mobille.dto.assistance.TypeAssistanceResponseDto;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrentContextService {

    private final SessionService sessionService;
    private final ExerciceService exerciceService;
    private final AssistanceService assistanceService;
    private final MutuelleConfigService mutuelleConfigService;

    public CurrentContextResponseDTO buildCurrentContext() {
        // 1. Session courante
        Session currentSession = null;
        Exercice currentExercice = null;

        try {
            currentSession = sessionService.getCurrentSession();
            currentExercice = currentSession.getExercice();
        } catch (RuntimeException e) {
            // Pas grave si pas de session active → on laisse null
        }

        MutuelleConfig config = mutuelleConfigService.getCurrentConfig();

        // 3.types d'assistances
        List<TypeAssistanceResponseDto> typeAssistance  = assistanceService.getAllTypeAssistances();

        return CurrentContextResponseDTO.builder()
                .currentSession(currentSession != null ? sessionService.mapToResponseDTO(currentSession) : null)
                .currentExercice(currentExercice != null ? exerciceService.mapToResponseDTO(currentExercice) : null)
                .config(config)
                .typeAssistance(typeAssistance)
                .build();
    }
}