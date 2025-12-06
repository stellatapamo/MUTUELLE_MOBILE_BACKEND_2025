package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.account.AccountMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountMemberRepository extends JpaRepository<AccountMember, Long> {
    Optional<AccountMember> findByMemberId(Long memberId);
}