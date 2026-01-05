package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.CurrentContextResponseDTO;
import com.mutuelle.mobille.dto.assistance.TypeAssistanceResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.models.BorrowingCeilingInterval;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrentContextService {

    private final SessionService sessionService;
    private final ExerciceService exerciceService;
    private final AssistanceService assistanceService;
    private final MutuelleConfigService mutuelleConfigService;
    private final BorrowingCeilingService borrowingCeilingService;

    public CurrentContextResponseDTO buildCurrentContext() {

        // 1. Récupérer la session courante (optionnelle)
        Optional<Session> optionalSession = sessionService.getCurrentSession();

        // 2. Mapper la session courante en DTO (ou null si absente)
        SessionResponseDTO currentSessionDto = optionalSession
                .map(sessionService::mapToResponseDTO)
                .orElse(null);

        // 3. Déterminer l'exercice courant
        // Priorité 1 : celui de la session courante
        // Priorité 2 : un exercice en cours même sans session active
        ExerciceResponseDTO currentExerciceDto = optionalSession
                .map(Session::getExercice)
                .map(exerciceService::mapToResponseDTO)
                .orElseGet(() -> exerciceService.getCurrentExercice()
                        .map(exerciceService::mapToResponseDTO)
                        .orElse(null));

        // 4. Config et types d'assistance
        MutuelleConfig config = mutuelleConfigService.getCurrentConfig();
        List<TypeAssistanceResponseDto> typeAssistance = assistanceService.getAllTypeAssistances();

        // 5. Grille des plafonds d'emprunt
        List<BorrowingCeilingInterval> borrowingCeilingIntervals = borrowingCeilingService.getAllIntervalsOrdered() ;

        // 6. Construire la réponse
        return CurrentContextResponseDTO.builder()
                .currentSession(currentSessionDto)
                .currentExercice(currentExerciceDto)
                .config(config)
                .typeAssistance(typeAssistance)
                .borrowingCeilingIntervals(borrowingCeilingIntervals)
                .build();
    }
}