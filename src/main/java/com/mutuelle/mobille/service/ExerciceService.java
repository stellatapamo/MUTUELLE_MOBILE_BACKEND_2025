package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.exercice.ExerciceRequestDTO;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.repository.ExerciceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciceService {

    private final ExerciceRepository exerciceRepository;

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

    private ExerciceResponseDTO mapToResponseDTO(Exercice exercice) {
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
}