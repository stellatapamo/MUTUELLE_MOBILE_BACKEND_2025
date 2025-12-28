package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.session.SessionRequestDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.repository.ExerciceRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ExerciceRepository exerciceRepository;

    public List<SessionResponseDTO> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public SessionResponseDTO getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id : " + id));
        return mapToResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO createSession(SessionRequestDTO request) {
        Exercice exercice = exerciceRepository.findById(request.getExerciceId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé avec l'id : " + request.getExerciceId()));

        Session session = Session.builder()
                .name(request.getName())
                .solidarityAmount(request.getSolidarityAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .inProgress(request.isInProgress())
                .exercice(exercice)
                .build();

        session = sessionRepository.save(session);
        return mapToResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO updateSession(Long id, SessionRequestDTO request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec l'id : " + id));

        Exercice exercice = exerciceRepository.findById(request.getExerciceId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé avec l'id : " + request.getExerciceId()));

        session.setName(request.getName());
        session.setSolidarityAmount(request.getSolidarityAmount());
        session.setStartDate(request.getStartDate());
        session.setEndDate(request.getEndDate());
        session.setInProgress(request.isInProgress());
        session.setExercice(exercice);

        session = sessionRepository.save(session);
        return mapToResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("Session non trouvée avec l'id : " + id);
        }
        sessionRepository.deleteById(id);
    }

    public SessionResponseDTO mapToResponseDTO(Session session) {
        return SessionResponseDTO.builder()
                .id(session.getId())
                .name(session.getName())
                .solidarityAmount(session.getSolidarityAmount())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .inProgress(session.isInProgress())
                .exerciceId(session.getExercice().getId())
                .exerciceName(session.getExercice().getName())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
    /**
     * Retourne la session actuellement en cours (inProgress = true)
     */
    public Session getCurrentSession() {
        return sessionRepository.findByInProgressTrue()
                .orElseThrow(() ->
                        new RuntimeException("Aucune session active en cours"));
    }

}