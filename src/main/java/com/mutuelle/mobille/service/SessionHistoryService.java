package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.sessionHistory.SessionHistoryResponseDTO;
import com.mutuelle.mobille.models.SessionHistory;
import com.mutuelle.mobille.repository.ExerciceRepository;
import com.mutuelle.mobille.repository.SessionHistoryRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionHistoryService {

    private final SessionHistoryRepository sessionHistoryRepository;
    private final SessionRepository sessionRepository;
    private final ExerciceRepository exerciceRepository;

    public List<SessionHistoryResponseDTO> getAllHistory() {
        return sessionHistoryRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }



    public List<SessionHistoryResponseDTO> getHistoryByExerciceId(Long exerciceId) {
        return sessionHistoryRepository.findAllByExerciceId(exerciceId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SessionHistoryResponseDTO> getHistoryBySessionId(Long sessionId) {
        // 1. D'abord vérifier si la session existe
        if (!sessionRepository.existsById(sessionId)) {
            throw new RuntimeException("Session non trouvée avec l'id : " + sessionId);
        }

        Optional<SessionHistory> history = sessionHistoryRepository.findBySessionId(sessionId);

        // 3. Retourner une liste (vide si pas d'historique)
        return history.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private SessionHistoryResponseDTO toResponseDTO(SessionHistory h) {
        return SessionHistoryResponseDTO.builder()
                .id(h.getId())
                .sessionId(h.getSession().getId())
                .sessionName(h.getSession().getName())
                .sessionStartDate(h.getSession().getStartDate())
                .sessionEndDate(h.getSession().getEndDate())
                .totalAssistanceAmount(h.getTotalAssistanceAmount())
                .totalAssistanceCount(h.getTotalAssistanceCount())
                .agapeAmount(h.getAgapeAmount())
                .mutuelleCash(h.getMutuelleCash())
                .mutuellesSavingAmount(h.getMutuellesSavingAmount())
                .mutuelleBorrowAmount(h.getMutuelleBorrowAmount())
                .totalTransactions(h.getTotalTransactions())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
