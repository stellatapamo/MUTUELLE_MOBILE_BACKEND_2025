package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Renfoulement;
import com.mutuelle.mobille.models.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RenfoulementRepository extends JpaRepository<Renfoulement, Long> {

    Optional<Renfoulement> findByExerciceId(Long exerciceId);
}