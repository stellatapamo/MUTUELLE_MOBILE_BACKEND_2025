package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.exercice.ExerciceRequestDTO;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.repository.ExerciceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciceService {

    private final ExerciceRepository exerciceRepository;
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
                .amount(request.getAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        validateExerciceDates(exercice, null);

        exercice = exerciceRepository.save(exercice);
        return mapToResponseDTO(exercice);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciceResponseDTO updateExercice(Long id, ExerciceRequestDTO request) {
        Exercice exercice = exerciceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé avec l'id : " + id));

        exercice.setName(request.getName());
        exercice.setAmount(request.getAmount());
        exercice.setStartDate(request.getStartDate());
        exercice.setEndDate(request.getEndDate());

        validateExerciceDates(exercice, id);

        exercice = exerciceRepository.save(exercice);
        return mapToResponseDTO(exercice);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteExercice(Long id) {
        if (!exerciceRepository.existsById(id)) {
            throw new RuntimeException("Exercice non trouvé avec l'id : " + id);
        }
        exerciceRepository.deleteById(id);
    }

    public ExerciceResponseDTO mapToResponseDTO(Exercice exercice) {
        return ExerciceResponseDTO.builder()
                .id(exercice.getId())
                .name(exercice.getName())
                .amount(exercice.getAmount())
                .startDate(exercice.getStartDate())
                .endDate(exercice.getEndDate())
                .createdAt(exercice.getCreatedAt())
                .updatedAt(exercice.getUpdatedAt())
                .build();
    }

    @Transactional
    public Optional<Exercice> getCurrentExercice() {
        LocalDateTime now = LocalDateTime.now();
        return exerciceRepository.findCurrentExercice(now);
    }
}