package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // Ajout nécessaire pour Map/HashMap

// @Service indique à Spring que cette classe contient la logique métier
@Service
public class RenflouementService {

    // --- Constantes de votre projet (montants de référence) ---
    private static final double CONTRIBUTION_ANNUELLE = 150000.00;
    private static final double CONTRIBUTION_MINIMALE_ANNUELLE = 150000.00;
    private static final double AGAPE_MENSUEL = 45000.00;

    // Montants des dépenses pour les événements (pour référence, si besoin)
    private static final double DEPENSE_MARIAGE = 100000.00;
    private static final double DEPENSE_RETRAITE = 500000.00;
    private static final double DEPENSE_DECES_MALADIE_1M = 1000000.00;
    private static final double DEPENSE_MALADIE = 300000.00;
    private static final double DEPENSE_DECES_CONJOINT = 500000.00;
    private static final double DEPENSE_DECES_ENFANT = 300000.00;

    // Injection des Repositories
    @Autowired
    private EvenementRepository evenementRepository;
    
    @Autowired
    private ContributionRepository contributionRepository;
    
    // ----------------------------------------------------------------------
    // --- GESTION DES DÉPENSES (ÉVÉNEMENTS) ---
    // ----------------------------------------------------------------------

    public Evenement enregistrerDepense(Evenement evenement) {
        return evenementRepository.save(evenement);
    }
    
    public List<Evenement> getAllDepenses() {
        return evenementRepository.findAll();
    }
    
    public void supprimerEvenement(Long id) {
        evenementRepository.deleteById(id);
    }

    // ----------------------------------------------------------------------
    // --- GESTION DES CONTRIBUTIONS (PAIEMENTS) ---
    // ----------------------------------------------------------------------

    public Contribution enregistrerContribution(Contribution contribution) {
        return contributionRepository.save(contribution);
    }

    public List<Contribution> getAllContributions() {
        return contributionRepository.findAll();
    }

    /**
     * Calcule le total des contributions d'un membre pour une année donnée 
     * et vérifie s'il est en règle AVANT la date limite d'Avril.
     */
    public boolean verifierStatutMembre(String nomMembre, int anneeExercice) {
        
        // La date limite est le 30 Avril (vérification avant le 1er Mai).
        LocalDate dateLimite = LocalDate.of(anneeExercice, 5, 1);
        
        // 1. Récupère les contributions faites AVANT la date limite.
        List<Contribution> contributions = contributionRepository
            .findByNomMembreAndAnneeExerciceAndDatePaiementBefore(
                nomMembre, 
                anneeExercice, 
                dateLimite
            );
        
        // 2. Calcule la somme des contributions
        double totalPaye = contributions.stream()
            .mapToDouble(Contribution::getMontantPaye)
            .sum();

        // 3. Vérifie si le total atteint le minimum requis (150 000 FCFA)
        return totalPaye >= CONTRIBUTION_MINIMALE_ANNUELLE;
    }
    
    /**
     * Calcule et retourne le statut de paiement de tous les membres pour une année donnée.
     * REMARQUE: La liste des membres est codée en dur pour les besoins du test.
     */
    public List<Map<String, String>> getAllStatutsMembres(int anneeExercice) {
        
        // Liste SIMULÉE des membres (à remplacer par une requête DB si vous créez une table Membre)
        List<String> tousLesMembres = List.of(
            "Membre Alpha", 
            "Membre Beta", 
            "Membre Gamma",
            "Membre Delta",
            "Membre Epsilon"
        );
        
        List<Map<String, String>> listeStatuts = new java.util.ArrayList<>();
        
        for (String nomMembre : tousLesMembres) {
            
            boolean estEnRegle = verifierStatutMembre(nomMembre, anneeExercice);
            
            Map<String, String> statut = new java.util.HashMap<>();
            statut.put("nom", nomMembre);
            statut.put("statut_paiement", estEnRegle ? "OUI (En Règle)" : "NON (Hors Délai)");
            statut.put("annee", String.valueOf(anneeExercice));
            
            listeStatuts.add(statut);
        }
        
        return listeStatuts;
    }

    // ----------------------------------------------------------------------
    // --- CALCUL DU SOLDE GLOBAL ET DU RÉSUMÉ (NOUVELLE FONCTION) ---
    // ----------------------------------------------------------------------

    /**
     * Calcule le solde du fonds et fournit un résumé détaillé des composantes.
     */
    public ResumeFonds calculerResumeFonds(int nombreMembres) { // NOTE: Le nom de la méthode a changé
        
        // --- 1. Revenus et Dépense Fixe ---
        double renflouementAnnuelTheorique = nombreMembres * CONTRIBUTION_ANNUELLE; // 10 * 150 000 = 1 500 000
        double depenseAgapesAnnuelle = AGAPE_MENSUEL * 12; // 45 000 * 12 = 540 000
        
        // --- 2. Dépenses d'Événements (Variable) ---
        List<Evenement> toutesDepenses = evenementRepository.findAll();
        
        double totalDepensesEvenements = 0;
        List<Map<String, Object>> detailsDepenses = new java.util.ArrayList<>();
        
        for (Evenement depense : toutesDepenses) {
            totalDepensesEvenements += depense.getMontant();
            
            // Préparation du détail pour l'objet de résumé
            Map<String, Object> detail = new java.util.HashMap<>();
            detail.put("id", depense.getId());
            detail.put("motif", depense.getType() + " de " + depense.getBeneficiaire());
            detail.put("montant", depense.getMontant());
            detail.put("date", depense.getDate());
            detailsDepenses.add(detail);
        }

        // --- 3. Calcul du Solde Final ---
        double solde = renflouementAnnuelTheorique - totalDepensesEvenements - depenseAgapesAnnuelle;
        
        // --- 4. Retourne l'objet de résumé détaillé ---
        return new ResumeFonds(
            renflouementAnnuelTheorique, 
            depenseAgapesAnnuelle, 
            detailsDepenses, 
            totalDepensesEvenements, 
            solde
        );
    }
}