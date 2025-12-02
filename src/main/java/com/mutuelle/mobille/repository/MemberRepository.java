package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> { }