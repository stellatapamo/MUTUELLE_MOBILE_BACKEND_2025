package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.session.SessionRequestDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.ExerciceRepository;
import com.mutuelle.mobille.repository.SessionRepository;
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
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ExerciceRepository exerciceRepository;
    private final AccountService accountService;

    private static final LocalDateTime NOW = LocalDateTime.now();

    private void validateSession(Session session, Long excludeId) {
        LocalDateTime sessionStart = session.getStartDate();
        LocalDateTime sessionEnd = session.getEndDate();

        if (sessionEnd != null && sessionStart.isAfter(sessionEnd)) {
            throw new IllegalArgumentException("La date de début de la session doit être avant la date de fin");
        }

        Exercice exercice = session.getExercice();

        // 4. La session doit être contenue dans l'exercice
        if (sessionStart.isBefore(exercice.getStartDate()) ||
                (exercice.getEndDate() != null && sessionEnd != null && sessionEnd.isAfter(exercice.getEndDate())) ||
                (exercice.getEndDate() != null && sessionEnd == null)) {
            throw new IllegalArgumentException("Les dates de la session doivent être comprises dans les dates de l'exercice parent");
        }

        // 2. Pas de chevauchement global entre sessions
        boolean overlap = sessionRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                sessionEnd == null ? LocalDateTime.MAX : sessionEnd,
                sessionStart,
                excludeId != null ? excludeId : -1L);

        if (overlap) {
            throw new IllegalArgumentException("Cette session se chevauche avec une autre session existante");
        }

        // 1. Unicité de la session en cours
        Optional<Session> currentSession = sessionRepository.findCurrentSession(NOW);
        if (currentSession.isPresent() && !currentSession.get().getId().equals(excludeId)) {
            boolean newIsCurrent = (sessionEnd == null || sessionEnd.isAfter(NOW)) && sessionStart.isBefore(NOW);
            if (newIsCurrent) {
                throw new IllegalArgumentException("Une session est déjà en cours à cette période");
            }
        }
    }

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
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        Session session = Session.builder()
                .name(request.getName())
                .solidarityAmount(request.getSolidarityAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .inProgress(true) // par défaut une nouvelle session est active si elle chevauche "now"
                .exercice(exercice)
                .build();

        validateSession(session, null);

        session = sessionRepository.save(session);

        // Si la nouvelle session est immédiatement en cours → appliquer la cotisation
        if (session.isInProgress()) {
            applySolidarityAgapesToAllMembers(session);
        }

        return mapToResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO updateSession(Long id, SessionRequestDTO request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        boolean wasInProgress = session.isInProgress();

        Exercice exercice = exerciceRepository.findById(request.getExerciceId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        session.setName(request.getName());
        session.setSolidarityAmount(request.getSolidarityAmount());
        session.setStartDate(request.getStartDate());
        session.setEndDate(request.getEndDate());
        session.setExercice(exercice);

        validateSession(session, id);

        // Mise à jour automatique du flag inProgress
        boolean shouldBeInProgress = (session.getEndDate() == null || session.getEndDate().isAfter(NOW))
                && session.getStartDate().isBefore(NOW);
        session.setInProgress(shouldBeInProgress);

        session = sessionRepository.save(session);

        // Détecter le passage à "en cours" (manuellement ou par dates)
        if (!wasInProgress && shouldBeInProgress) {
            applySolidarityAgapesToAllMembers(session);
        }

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
     * Retourne la session actuellement en cours
     */
    @Transactional
    public Optional<Session> getCurrentSession() {
        LocalDateTime now = LocalDateTime.now();

        Optional<Session> optionalSession = sessionRepository.findCurrentSession(now);

        if (optionalSession.isPresent()) {
            Session session = optionalSession.get();

            boolean isCurrentlyActive = session.getStartDate().isBefore(now) || session.getStartDate().equals(now);
            if (session.getEndDate() != null) {
                isCurrentlyActive = isCurrentlyActive && !session.getEndDate().isBefore(now);
            }

            // Si le flag inProgress était faux et passe à vrai → c'est un nouveau démarrage
            boolean wasInProgress = session.isInProgress();
            if (!wasInProgress && isCurrentlyActive) {
                applySolidarityAgapesToAllMembers(session);
            }

            // Correction du flag si nécessaire
            if (session.isInProgress() != isCurrentlyActive) {
                session.setInProgress(isCurrentlyActive);
                sessionRepository.save(session);
            }

            return Optional.of(session);
        }

        return Optional.empty();
    }

    /**
     * Applique la cotisation solidarité et l'agapes  de la session à tous les membres actifs
     */
    @Transactional
    public void applySolidarityAgapesToAllMembers(Session session) {
        if (session.getSolidarityAmount() == null || session.getSolidarityAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Exercice exercice = session.getExercice();

        List<AccountMember> allAccounts = accountService.getAllMemberAccounts();

        for (AccountMember account : allAccounts) {
            if (account.isActive()) {
                BigDecimal currentUnpaid = account.getUnpaidSolidarityAmount();
                if (currentUnpaid == null) {
                    currentUnpaid = BigDecimal.ZERO;
                }
                account.setUnpaidSolidarityAmount(currentUnpaid.add(session.getSolidarityAmount()));

                BigDecimal currentUnpaidAgape = account.getUnpaidAgapesAmount();
                if (currentUnpaidAgape == null) {
                    currentUnpaidAgape = BigDecimal.ZERO;
                }
                account.setUnpaidAgapesAmount(currentUnpaidAgape.add(exercice.getAgapeAmount()));
            }
        }

        // Sauvegarde en batch si possible, ou via saveAll si tu ajoutes la méthode
        accountService.getAllMemberAccounts().forEach(accountService::saveMemberAccount);
        // Ou mieux : ajouter une méthode saveAll dans AccountMemberRepository
    }

}