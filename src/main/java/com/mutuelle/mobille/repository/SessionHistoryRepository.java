package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.SessionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionHistoryRepository extends JpaRepository<SessionHistory, Long> {
    boolean existsBySessionId(Long sessionId);
}