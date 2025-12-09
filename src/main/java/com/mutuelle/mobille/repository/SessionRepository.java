package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    // Spring génère automatiquement :
    // - findById(Long id)
    // - findAll()
    // - save(Session session)
    // - delete(Session session)
}