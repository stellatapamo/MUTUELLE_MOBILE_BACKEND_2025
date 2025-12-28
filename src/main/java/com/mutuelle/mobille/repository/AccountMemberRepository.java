package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.account.AccountMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountMemberRepository extends JpaRepository<AccountMember, Long> {
    Optional<AccountMember> findByMemberId(Long memberId);

    // Membres à jour = inscription totalement payée
    List<AccountMember> findByUnpaidRegistrationAmount(BigDecimal amount);
}