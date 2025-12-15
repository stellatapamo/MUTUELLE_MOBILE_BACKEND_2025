package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}