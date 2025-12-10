package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    
    /**
     * Trouve toutes les contributions d'un membre spécifique pour une année d'exercice donnée.
     * @param nomMembre Le nom du membre.
     * @param anneeExercice L'année fiscale de la contribution.
     * @return Liste des contributions correspondantes.
     */
    List<Contribution> findByNomMembreAndAnneeExercice(String nomMembre, int anneeExercice);

    /**
     * Trouve toutes les contributions d'un membre pour une année donnée 
     * qui ont été payées avant une date limite spécifique (pour la règle d'Avril).
     * @param nomMembre Le nom du membre.
     * @param anneeExercice L'année fiscale de la contribution.
     * @param dateLimite La date de paiement maximale autorisée.
     * @return Liste des contributions faites avant la date limite.
     */
    List<Contribution> findByNomMembreAndAnneeExerciceAndDatePaiementBefore(
        String nomMembre, 
        int anneeExercice,
        LocalDate dateLimite
    );
}