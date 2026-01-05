package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.List;
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    /**
     * Retourne la session actuellement en cours (inProgress = true)
     */
    Optional<Session> findByInProgressTrue();
    // Spring génère automatiquement :
    // - findById(Long id)
    // - findAll()
    // - save(Session session)
    // - delete(Session session)

    // récupérer les sessions par exercice
    List<Session> findByExerciceId(Long exerciceId);

    @Query("SELECT s FROM Session s WHERE s.startDate <= :now AND (s.endDate IS NULL OR s.endDate >= :now)")
    Optional<Session> findCurrentSession(LocalDateTime now);

    // Pour vérifier le chevauchement global des sessions
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDateTime end, LocalDateTime start, Long excludeId);
}
