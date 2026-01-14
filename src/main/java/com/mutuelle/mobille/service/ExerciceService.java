package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.exercice.ExerciceRequestDTO;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.ExerciceHistory;
import com.mutuelle.mobille.repository.ExerciceHistoryRepository;
import com.mutuelle.mobille.repository.ExerciceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciceService {

    private final ExerciceRepository exerciceRepository;
    private final ExerciceHistoryRepository exerciceHistoryRepository;
    private static final LocalDateTime NOW = LocalDateTime.now();

    private void validateExerciceDates(Exercice exercice, Long excludeId) {
        LocalDateTime start = exercice.getStartDate();
        LocalDateTime end = exercice.getEndDate();

        if (end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("La date de début doit être antérieure ou égale à la date de fin");
        }

        // Vérifier chevauchement avec un autre exercice
        boolean overlap = exerciceRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(end, start, excludeId != null ? excludeId : -1L)
                || (excludeId == null && exerciceRepository.existsByStartDateBetweenOrEndDateBetween(start, end, start, end));

        if (overlap) {
            throw new IllegalArgumentException("Les dates de cet exercice se chevauchent avec un autre exercice existant");
        }

        // Vérifier unicité de l'exercice en cours
        Optional<Exercice> current = exerciceRepository.findCurrentExercice(NOW);
        if (current.isPresent() && !current.get().getId().equals(excludeId)) {
            LocalDateTime currentStart = current.get().getStartDate();
            LocalDateTime currentEnd = current.get().getEndDate();
            boolean newIsCurrent = (end == null || end.isAfter(NOW)) && start.isBefore(NOW);

            if (newIsCurrent) {
                throw new IllegalArgumentException("Un exercice est déjà en cours à cette période");
            }
        }
    }

    public List<ExerciceResponseDTO> getAllExercices() {
        return exerciceRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ExerciceResponseDTO getExerciceById(Long id) {
        Exercice exercice = exerciceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé avec l'id : " + id));
        return mapToResponseDTO(exercice);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciceResponseDTO createExercice(ExerciceRequestDTO request) {
        Exercice exercice = Exercice.builder()
                .name(request.getName())
                .agapeAmount(request.getAgapeAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        validateExerciceDates(exercice, null);

        if (exercice.isInProgress()) {
            onExerciceStarted(exercice);
        }
        exercice = exerciceRepository.save(exercice);
        return mapToResponseDTO(exercice);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciceResponseDTO updateExercice(Long id, ExerciceRequestDTO request) {
        Exercice exercice = exerciceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé avec l'id : " + id));

        if (exercice.getHistory() != null) {
            throw new IllegalStateException("Impossible de mettre à jour un exercice fermé avec historique");
        }

        boolean wasInProgress = exercice.isInProgress();

        exercice.setName(request.getName());
        exercice.setAgapeAmount(request.getAgapeAmount());
        exercice.setStartDate(request.getStartDate());
        exercice.setEndDate(request.getEndDate());

        boolean shouldBeInProgress = (exercice.getEndDate() == null || exercice.getEndDate().isAfter(NOW))
                && exercice.getStartDate().isBefore(NOW);
        exercice.setInProgress(shouldBeInProgress);

        validateExerciceDates(exercice, id);

        exercice = exerciceRepository.save(exercice);

        if (!wasInProgress && shouldBeInProgress) {
            onExerciceStarted(exercice);
        } else if (wasInProgress && !shouldBeInProgress) {
            onExerciceEnded(exercice);
        }

        return mapToResponseDTO(exercice);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteExercice(Long id) {
        Exercice exercice = exerciceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé avec l'id : " + id));

        if (exercice.getHistory() != null) {
            throw new IllegalStateException("Impossible de supprimer un exercice fermé avec historique");
        }

        exerciceRepository.deleteById(id);
    }

    public ExerciceResponseDTO mapToResponseDTO(Exercice exercice) {
        return ExerciceResponseDTO.builder()
                .id(exercice.getId())
                .name(exercice.getName())
                .agapeAmount(exercice.getAgapeAmount())
                .startDate(exercice.getStartDate())
                .endDate(exercice.getEndDate())
                .createdAt(exercice.getCreatedAt())
                .updatedAt(exercice.getUpdatedAt())
                .build();
    }

    /**
     * Méthode appelée quand un exercice se termine (n'est plus en cours)
     */
    @Transactional
    public void onExerciceEnded(Exercice exercice) {
        if (exercice.getHistory() != null) {
            return; // Déjà fermé
        }

        ExerciceHistory history = ExerciceHistory.builder()
                .exercice(exercice)
                .totalAssistanceAmount(BigDecimal.ZERO)
                .totalAssistanceCount(0)
                .build();

        exercice.setHistory(history);
        exerciceRepository.save(exercice); // Cascade sauvegarde l'historique
    }

    /**
     * Méthode appelée quand un exercice passe à l'état "en cours"
     */
    @Transactional
    public void onExerciceStarted(Exercice exercice) {
        // TODO : implémenter la logique quand un exercice démarre
        // Exemples possibles :
        // - Envoyer une notification aux membres
        // - Initialiser des compteurs
        // - Créer des entrées comptables
        // - etc.
        System.out.println("Exercice démarré : " + exercice.getName() + " (ID: " + exercice.getId() + ")");
    }

    /**
     * Retourne l'exercice actuellement en cours (avec mise à jour automatique du flag)
     */
    @Transactional
    public Optional<Exercice> getCurrentExercice() {
        LocalDateTime now = LocalDateTime.now();
        Optional<Exercice> optionalExercice = exerciceRepository.findCurrentExercice(now);

        if (optionalExercice.isPresent()) {
            Exercice exercice = optionalExercice.get();
            boolean wasInProgress = exercice.isInProgress();

            // Le flag est déjà mis à jour par @PreUpdate, mais on vérifie au cas où
            if (!wasInProgress && exercice.isInProgress()) {
                onExerciceStarted(exercice);
            } else if (wasInProgress && !exercice.isInProgress()) {
                onExerciceEnded(exercice);
            }

            return Optional.of(exercice);
        }
        return Optional.empty();
    }
}