package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> { }