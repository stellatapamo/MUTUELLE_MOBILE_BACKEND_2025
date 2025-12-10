package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository indique à Spring que cette interface est la couche d'accès aux données.
@Repository
// JpaRepository<Evenement, Long> fournit toutes les méthodes CRUD (save, findAll, findById, delete, etc.)
public interface EvenementRepository extends JpaRepository<Evenement, Long> {

    // Ici, vous pouvez ajouter des méthodes de recherche personnalisées si nécessaire,
    // comme : List<Evenement> findByBeneficiaire(String beneficiaire);
    // Spring Data JPA les implémente automatiquement.
}