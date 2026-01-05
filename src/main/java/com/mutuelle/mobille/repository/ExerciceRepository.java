package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    // Trouver l'exercice dont la période contient la date donnée
    @Query("SELECT e FROM Exercice e WHERE e.startDate <= :now AND (e.endDate IS NULL OR e.endDate >= :now)")
    Optional<Exercice> findCurrentExercice(LocalDateTime now);

    // Vérifier s'il existe un exercice qui chevauche les dates données (excluant l'id fourni pour les updates)
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDateTime end, LocalDateTime start, Long excludeId);

    boolean existsByStartDateBetweenOrEndDateBetween(
            LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2);
}