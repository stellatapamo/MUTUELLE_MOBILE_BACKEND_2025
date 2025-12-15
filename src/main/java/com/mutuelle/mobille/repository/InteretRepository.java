package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Interet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteretRepository extends JpaRepository<Interet, Long> {
}
