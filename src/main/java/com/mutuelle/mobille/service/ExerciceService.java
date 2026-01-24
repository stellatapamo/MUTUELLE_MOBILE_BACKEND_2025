package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.exercice.ExerciceRequestDTO;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.enums.StatusExercice;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.ExerciceHistory;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.SessionHistory;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.ExerciceHistoryRepository;
import com.mutuelle.mobille.repository.ExerciceRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
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
    private final SessionRepository sessionRepository;
    private final AccountService accountService;
    private final RenfoulementService renfoulementService;


    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // ───────────────────────────────────────────────
    //              Validation centrale
    // ───────────────────────────────────────────────

    private void validateExerciceDates(Exercice exercice, Long excludeId) {
        LocalDateTime start = exercice.getStartDate();
        LocalDateTime end = exercice.getEndDate();

        if (end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Date de début doit être ≤ date de fin");
            }

            if (start.toLocalDate().equals(end.toLocalDate())) {
                throw new IllegalArgumentException(
                        "L'exercice doit couvrir au moins deux jours différents " +
                                "(date de début et date de fin ne peuvent pas être le même jour)"
                );
            }
        }

        // Chevauchement avec un autre exercice
        boolean overlap = exerciceRepository.existsOverlapping(
                start,
                end != null ? end : LocalDateTime.MAX,
                excludeId != null ? excludeId : null
        );

        if (overlap) {
            throw new IllegalArgumentException("les plages de date de l'exercice coincide avec une autre");
        }

        // Unicité IN_PROGRESS
        boolean wouldBeInProgress = !start.isAfter(now()) && (end == null || !end.isBefore(now()));
        if (wouldBeInProgress && excludeId != null) {
            Optional<Exercice> other = exerciceRepository.findFirstByStatus(StatusExercice.IN_PROGRESS);
            if (other.isPresent() && !other.get().getId().equals(excludeId)) {
                throw new IllegalStateException("Un autre exercice est déjà IN_PROGRESS");
            }
        }

        // Interdire modification si historisé
        if (excludeId != null && exerciceHistoryRepository.existsByExerciceId(excludeId)) {
            throw new IllegalStateException("Exercice déjà clôturé (historique existant)");
        }
    }

    // ───────────────────────────────────────────────
    //              CRUD
    // ───────────────────────────────────────────────

    public List<ExerciceResponseDTO> getAllExercices() {
        return exerciceRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ExerciceResponseDTO getExerciceById(Long id) {
        Exercice ex = exerciceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé : " + id));
        return toResponseDTO(ex);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciceResponseDTO createExercice(ExerciceRequestDTO request) {
        Exercice exercice = Exercice.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(StatusExercice.PLANNED)
                .build();

        validateExerciceDates(exercice, null);
        exercice = exerciceRepository.save(exercice);
        return toResponseDTO(exercice);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciceResponseDTO updateExercice(Long id, ExerciceRequestDTO request) {
        Exercice exercice = exerciceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        StatusExercice currentStatus = exercice.getStatus();
        if (currentStatus == StatusExercice.COMPLETED || currentStatus == StatusExercice.CANCELLED) {
            throw new IllegalStateException("Modification interdite sur exercice terminé ou annulé");
        }

        // Mise à jour des champs modifiables
        if (request.getName() != null) exercice.setName(request.getName());

        // Gestion stricte des dates
        if (currentStatus == StatusExercice.IN_PROGRESS) {
            // startDate interdit
            if (request.getStartDate() != null && !request.getStartDate().equals(exercice.getStartDate())) {
                throw new IllegalArgumentException("Modification de startDate interdite sur exercice en cours");
            }
            // endDate → prolongation uniquement
            if (request.getEndDate() != null) {
                LocalDateTime newEnd = request.getEndDate();
                if (newEnd.isBefore(exercice.getEndDate())) {
                    throw new IllegalArgumentException("Raccourcissement d'un exercice en cours interdit");
                }
                if (newEnd.isBefore(now())) {
                    throw new IllegalArgumentException("Nouvelle date de fin doit être dans le futur");
                }
                exercice.setEndDate(newEnd);
            }
        } else {
            if (request.getStartDate() != null) exercice.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) exercice.setEndDate(request.getEndDate());
        }

        Exercice fakeClone = Exercice.builder()
                .startDate(request.getStartDate() != null ? request.getStartDate() : exercice.getStartDate())
                .endDate(request.getEndDate() != null ? request.getEndDate() : exercice.getEndDate())
                .build();

        validateExerciceDates(fakeClone, id);
        exercice = exerciceRepository.save(exercice);
        return toResponseDTO(exercice);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteExercice(Long id) {
        Exercice ex = exerciceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        // Nouvelle règle : Interdire si en cours ou terminé
        if (ex.getStatus() == StatusExercice.IN_PROGRESS || ex.getStatus() == StatusExercice.COMPLETED) {
            throw new IllegalStateException("Impossible de supprimer un exercice en cours ou terminé");
        }

        // Sécurité supplémentaire existante (historique)
        if (exerciceHistoryRepository.existsByExerciceId(id)) {
            throw new IllegalStateException("Impossible de supprimer un exercice historisé");
        }

        exerciceRepository.delete(ex);
    }

    // ───────────────────────────────────────────────
    //              Méthodes lecture / état
    // ───────────────────────────────────────────────

    public Optional<Exercice> findCurrentExercice() {
        LocalDateTime n = now();
        return exerciceRepository.findCurrentActiveExercice(StatusExercice.IN_PROGRESS,n);
    }

    public Optional<ExerciceResponseDTO> getCurrentExerciceDTO() {
        return findCurrentExercice().map(this::toResponseDTO);
    }

    // ───────────────────────────────────────────────
    //              Actions (appelées par le scheduler uniquement)
    // ───────────────────────────────────────────────

    @Transactional
    public void startExerciceIfDue(Exercice exercice) {
        if (exercice.getStatus() != StatusExercice.PLANNED) return;

        LocalDateTime n = now();
        if (!exercice.getStartDate().isAfter(n)) {
            exercice.setStatus(StatusExercice.IN_PROGRESS);
            exerciceRepository.save(exercice);
            onExerciceStarted(exercice);
        }
    }

    @Transactional
    public void closeExerciceIfExpired(Exercice exercice) {
        if (exercice.getStatus() != StatusExercice.IN_PROGRESS) return;

        LocalDateTime n = now();
        if (exercice.getEndDate() != null && exercice.getEndDate().isBefore(n)) {
            exercice.setStatus(StatusExercice.COMPLETED);
            exerciceRepository.save(exercice);
            onExerciceEnded(exercice);
        }
    }

    @Transactional
    public void onExerciceStarted(Exercice exercice) {
        // Logique métier au démarrage (notifications, initialisations, etc.)
        // Pour l'instant : trace simple
        System.out.println("Exercice démarré : " + exercice.getName() + " (ID: " + exercice.getId() + ")");
        // → Ajouter ici : envoi mail, création compteur, etc.
    }

    @Transactional
    public void onExerciceEnded(Exercice exercice) {
        if (exercice.getHistory() != null) return;
        AccountMutuelle mutuelleacc=accountService.getMutuelleGlobalAccount();

        List<Session> sessions = sessionRepository.findByExerciceId(exercice.getId());

        BigDecimal totalAssistanceAmount = BigDecimal.ZERO;
        Long totalAssistanceCount = 0L;
        BigDecimal totalAgapeAmount = BigDecimal.ZERO;
        BigDecimal mutuelleCash = BigDecimal.ZERO;
        Long totalTransactions = 0L;
        BigDecimal mutuellesSavingAmount = BigDecimal.ZERO;
        BigDecimal mutuelleBorrowAmount = BigDecimal.ZERO;

        for (Session session : sessions) {
            if (session.getHistory() != null) {
                SessionHistory hist = session.getHistory();
                totalAssistanceAmount = totalAssistanceAmount.add(hist.getTotalAssistanceAmount() != null ? hist.getTotalAssistanceAmount() : BigDecimal.ZERO);
                totalAssistanceCount += hist.getTotalAssistanceCount() != null ? hist.getTotalAssistanceCount() : 0L;
                totalAgapeAmount = totalAgapeAmount.add(hist.getAgapeAmount() != null ? hist.getAgapeAmount() : BigDecimal.ZERO);
                totalTransactions += hist.getTotalTransactions() != null ? hist.getTotalTransactions() : 0L;
               }
        }

        mutuelleCash = mutuelleCash.add(mutuelleacc.getTotalRenfoulement().add(mutuelleacc.getSolidarityAmount()));
        mutuellesSavingAmount = mutuelleacc.getSavingAmount();
        mutuelleBorrowAmount = mutuelleacc.getBorrowAmount();

        ExerciceHistory history = ExerciceHistory.builder()
                .exercice(exercice)
                .totalAssistanceAmount(totalAssistanceAmount)
                .totalAssistanceCount(totalAssistanceCount)
                .totalAgapeAmount(totalAgapeAmount)
                .mutuelleCash(mutuelleCash)
                .totalTransactions(totalTransactions)
                .mutuellesSavingAmount(mutuellesSavingAmount)
                .mutuelleBorrowAmount(mutuelleBorrowAmount)
                .build();

        exercice.setHistory(history);
        exerciceRepository.save(exercice);

//        renfoulement
        renfoulementService.calculateAndAssignRenfoulementForExercice(exercice);

    }

    public ExerciceResponseDTO toResponseDTO(Exercice ex) {
        return ExerciceResponseDTO.builder()
                .id(ex.getId())
                .name(ex.getName())
                .startDate(ex.getStartDate())
                .endDate(ex.getEndDate())
                .status(ex.getStatus())
                .createdAt(ex.getCreatedAt())
                .updatedAt(ex.getUpdatedAt())
                .build();
    }
}