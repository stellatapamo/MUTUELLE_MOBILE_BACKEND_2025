package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Assistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistanceRepository extends JpaRepository<Assistance, Long> {

    // Toutes les assistances d’un membre
    List<Assistance> findByMemberId(Long memberId);

    // Toutes les assistances d’une session
    List<Assistance> findBySessionId(Long sessionId);

    // Assistances par type d’assistance
    List<Assistance> findByTypeAssistanceId(Long typeAssistanceId);

    // Assistances par membre + session
    List<Assistance> findByMemberIdAndSessionId(Long memberId, Long sessionId);

    @Query("""
        SELECT COALESCE(SUM(a.typeAssistance.amount), 0)
        FROM Assistance a
        WHERE a.createdAt BETWEEN :start AND :end
    """)
    BigDecimal sumAssistancesBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
