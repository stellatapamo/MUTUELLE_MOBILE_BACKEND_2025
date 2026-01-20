package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.dto.session.SessionRequestDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.enums.*;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.SessionHistory;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.*;
import com.mutuelle.mobille.service.notifications.SessionNotificationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ExerciceRepository exerciceRepository;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AssistanceService assistanceService;
    private final SessionNotificationHelper notificationHelper;


//    private final Clock clock;  // ← à injecter (configurable pour les tests)

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // ───────────────────────────────────────────────
    //              Validation centrale
    // ───────────────────────────────────────────────

    private void validateSession(Session session, Long excludeId) {
        LocalDateTime start = session.getStartDate();
        LocalDateTime end = session.getEndDate();
        LocalDateTime now = now();

        if (end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Date de début doit être ≤ date de fin");
            }

            if (start.toLocalDate().equals(end.toLocalDate())) {
                throw new IllegalArgumentException(
                        "La session doit couvrir au moins deux jours différents " +
                                "(date de début et date de fin ne peuvent pas être le même jour)"
                );
            }
        }

        Exercice ex = session.getExercice();
        if (ex == null) {
            throw new IllegalArgumentException("Exercice parent obligatoire");
        }

        // Session doit être contenue dans l'exercice
        if (start.isBefore(ex.getStartDate()) ||
                (ex.getEndDate() != null && end != null && end.isAfter(ex.getEndDate()))) {
            throw new IllegalArgumentException("Session hors des dates de l'exercice parent");
        }

        // Pas de chevauchement avec d'autres sessions (tous statuts confondus)
        boolean overlap = sessionRepository.existsOverlapping(
                start,
                end != null ? end : LocalDateTime.MAX,
                excludeId != null ? excludeId : null
        );

        if (overlap) {
            throw new IllegalArgumentException("les plages de date de la session coincide avec une autre ");
        }

        // Si la session est ou devient IN_PROGRESS → vérifier qu'aucune autre ne l'est déjà
        boolean wouldBeInProgress = !start.isAfter(now) && (end == null || !end.isBefore(now));
        if (wouldBeInProgress && excludeId != null) {
            Optional<Session> other = sessionRepository.findFirstByStatus(StatusSession.IN_PROGRESS);
            if (other.isPresent() && !other.get().getId().equals(excludeId)) {
                throw new IllegalStateException("Une autre session est déjà IN_PROGRESS");
            }
        }

        // Interdire modification si déjà historisée
        if (excludeId != null && sessionHistoryRepository.existsBySessionId(excludeId)) {
            throw new IllegalStateException("Session déjà clôturée (historique existant)");
        }
    }

    // ───────────────────────────────────────────────
    //              CRUD de base
    // ───────────────────────────────────────────────

    public List<SessionResponseDTO> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SessionResponseDTO getSessionById(Long id) {
        Session s = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée : " + id));
        return toResponseDTO(s);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO createSession(SessionRequestDTO dto) {
        Exercice ex = exerciceRepository.findById(dto.getExerciceId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        Session session = Session.builder()
                .name(dto.getName())
                .solidarityAmount(dto.getSolidarityAmount())
                .agapeAmountPerMember(dto.getAgapeAmountPerMember())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(StatusSession.PLANNED)
                .exercice(ex)
                .build();

        validateSession(session, null);
        session = sessionRepository.save(session);
        return toResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO updateSession(Long id, SessionRequestDTO dto) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        StatusSession currentStatus = session.getStatus();
        if (currentStatus == StatusSession.COMPLETED || currentStatus == StatusSession.CANCELLED) {
            throw new IllegalStateException("Modification interdite sur session terminée ou annulée");
        }

        // Mise à jour des champs modifiables
        if (dto.getName() != null) session.setName(dto.getName());
        if (dto.getSolidarityAmount() != null) session.setSolidarityAmount(dto.getSolidarityAmount());
        if (dto.getAgapeAmountPerMember() != null) session.setAgapeAmountPerMember(dto.getAgapeAmountPerMember());

        // Gestion stricte des dates
        if (currentStatus == StatusSession.IN_PROGRESS) {
            // startDate interdit
            if (dto.getStartDate() != null && !dto.getStartDate().equals(session.getStartDate())) {
                throw new IllegalArgumentException("Modification de startDate interdite sur session en cours");
            }
            // endDate → prolongation uniquement
            if (dto.getEndDate() != null) {
                LocalDateTime newEnd = dto.getEndDate();
                if (newEnd.isBefore(session.getEndDate())) {
                    throw new IllegalArgumentException("Raccourcissement de session en cours interdit");
                }
                if (newEnd.isBefore(now())) {
                    throw new IllegalArgumentException("Nouvelle fin doit être dans le futur");
                }
                session.setEndDate(newEnd);
            }
        } else {
            if (dto.getStartDate() != null) session.setStartDate(dto.getStartDate());
            if (dto.getEndDate() != null) session.setEndDate(dto.getEndDate());
        }

        Session fakeClone = Session.builder()
                .startDate(dto.getStartDate() != null ? dto.getStartDate() : session.getStartDate())
                .endDate(dto.getEndDate() != null ? dto.getEndDate() : session.getEndDate())
                .build();

        validateSession(fakeClone, id);
        session = sessionRepository.save(session);
        return toResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSession(Long id) {
        Session s = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        // Nouvelle règle : Interdire si en cours ou terminée
        if (s.getStatus() == StatusSession.IN_PROGRESS || s.getStatus() == StatusSession.COMPLETED) {
            throw new IllegalStateException("Impossible de supprimer une session en cours ou terminée");
        }

        // Sécurité supplémentaire existante (historique)
        if (sessionHistoryRepository.existsBySessionId(id)) {
            throw new IllegalStateException("Impossible de supprimer une session historisée");
        }

        sessionRepository.delete(s);
    }

    // ───────────────────────────────────────────────
    //              Méthodes lecture / état
    // ───────────────────────────────────────────────

    public Optional<Session> findCurrentSession() {
        return sessionRepository.findCurrentInProgress(LocalDateTime.now());
    }

    public Optional<SessionResponseDTO> getCurrentSessionDTO() {
        return findCurrentSession().map(this::toResponseDTO);
    }

    // ───────────────────────────────────────────────
    //              Actions importantes (appelées par scheduler)
    // ───────────────────────────────────────────────

    @Transactional
    public void startSessionIfDue(Session session) {
        if (session.getStatus() != StatusSession.PLANNED) return;

        LocalDateTime n = now();
        if (!session.getStartDate().isAfter(n)) {
            session.setStatus(StatusSession.IN_PROGRESS);
            sessionRepository.save(session);
            applySolidarityToAllMembers(session);
            notificationHelper.notifySessionStarted(session);
        }
    }

    @Transactional
    public void closeSessionIfExpired(Session session) {
        if (session.getStatus() != StatusSession.IN_PROGRESS) return;

        LocalDateTime n = now();
        if (session.getEndDate() != null && session.getEndDate().isBefore(n)) {
//            session.setStatus(StatusSession.COMPLETED);
//            sessionRepository.save(session);
//            onSessionEnded(session);
            try {
                session.setStatus(StatusSession.COMPLETED);
                onSessionEnded(session);

                // Notification de fin de session
                notificationHelper.notifySessionEnded(session);

            } catch (IllegalArgumentException e) {
                session.setStatus(StatusSession.IN_PROGRESS);
                sessionRepository.save(session);

                notificationHelper.notifyAdminCritical(
                        "Échec clôture session " + session.getName(),
                        "Impossible de clôturer - caisse solidarité insuffisante",
                        e
                );

                throw e; // on laisse remonter l'exception
            }
        }
    }

    @Transactional
    public void applySolidarityToAllMembers(Session session) {
        if (session.getSolidarityAmount() == null || session.getSolidarityAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        List<AccountMember> activeMembers = accountService.getAllMemberAccounts().stream()
                .filter(AccountMember::isActive)
                .toList();

        for (AccountMember m : activeMembers) {
            BigDecimal unpaid = m.getUnpaidSolidarityAmount();
            if (unpaid == null) unpaid = BigDecimal.ZERO;
            m.setUnpaidSolidarityAmount(unpaid.add(session.getSolidarityAmount()));
            accountService.saveMemberAccount(m);
        }

//        notificationHelper.notifySolidarityApplied(session);
    }

    @Transactional
    public void onSessionEnded(Session session) {
        if (session.getHistory() != null) return;
        AccountMutuelle mutuelleacc=accountService.getMutuelleGlobalAccount();
        Long sessionId=session.getId();

        // Débit agapes
        List<AccountMember> active = accountService.getAllMemberAccountsWithActive(true);
        BigDecimal perMember = session.getAgapeAmountPerMember();
        BigDecimal totalDebit = perMember.multiply(BigDecimal.valueOf(active.size()));

        BigDecimal currentSolidarityBalance = mutuelleacc.getSolidarityAmount();

        if (currentSolidarityBalance.compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Impossible de débiter les agapes pour la session '%s' : " +
                                    "caisse solidarité insuffisante.\n" +
                                    "→ Montant requis : %s\n" +
                                    "→ Solde actuel  : %s\n" +
                                    "→ Écart         : %s\n" +
                            session.getName(),
                            totalDebit,
                            currentSolidarityBalance,
                            totalDebit.subtract(currentSolidarityBalance)
                    )
            );
        }

        accountService.removeToSolidarityMutuelleCaisse(totalDebit);

        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.AGAPE)
                .amount(totalDebit)
                .description("Agapes session " + session.getName())
                .transactionDirection(TransactionDirection.DEBIT)
                .accountMember(null)
                .session(session)
                .build();
        transactionRepository.save(tx);

        sessionRepository.save(session);

        SessionHistory history = SessionHistory.builder()
                .session(session)
                .totalAssistanceAmount(assistanceService.getTotalAssistanceAmountForSession(sessionId))
                .totalAssistanceCount(assistanceService.countTotalAssistanceForSession(sessionId))
                .agapeAmount(totalDebit)
                .totalTransactions(transactionRepository.countBySessionId(sessionId))
                .mutuellesSavingAmount(mutuelleacc.getSavingAmount())
                .mutuelleCash(mutuelleacc.getTotalRenfoulement().add(mutuelleacc.getTotalRegistrationAmount().add(mutuelleacc.getSolidarityAmount())))
                .mutuelleBorrowAmount(mutuelleacc.getBorrowAmount())
                .build();

        session.setHistory(history);
    }

    public SessionResponseDTO toResponseDTO(Session s) {
        return SessionResponseDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .solidarityAmount(s.getSolidarityAmount())
                .agapeAmountPerMember(s.getAgapeAmountPerMember())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus())
                .exerciceId(s.getExercice().getId())
                .exerciceName(s.getExercice().getName())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }


}