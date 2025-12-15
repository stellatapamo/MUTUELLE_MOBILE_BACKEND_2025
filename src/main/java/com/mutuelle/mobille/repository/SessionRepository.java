package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    // Récupérer la session en cours (il ne doit y en avoir qu'une seule)
    Optional<Session> findByInProgressTrue();

    // récupérer les sessions par exercice
    List<Session> findByExerciceId(Long exerciceId);

}
