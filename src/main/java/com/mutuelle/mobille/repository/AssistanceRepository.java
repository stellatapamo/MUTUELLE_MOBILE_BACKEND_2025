package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Assistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistanceRepository extends JpaRepository<Assistance, Long> {

    // Utile pour récupérer les demandes d'un membre
    List<Assistance> findByMemberId(Long memberId);

    // Ou par session
    List<Assistance> findBySessionId(Long sessionId);

    // Par membre et session (si besoin)
    List<Assistance> findByMemberIdAndSessionId(Long memberId, Long sessionId);

    //  Nombre d'assistances pour un membre
    Long countByMemberId(Long memberId);

    //   Nombre d'assistances pour une session
    Long countBySessionId(Long sessionId);
}