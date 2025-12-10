package com.example.demo;

import java.util.List;
import java.util.Map; // Importation pour la liste détaillée des dépenses

public class ResumeFonds {

    private double renflouementInitial; // 1 500 000 FCFA
    private double depenseAgapesAnnuelle; // 540 000 FCFA
    private List<Map<String, Object>> detailsDepensesEvenements; // Liste des dépenses (Motif, Montant)
    private double totalDepenses; // Somme des dépenses d'événements
    private double soldeActuel; // Le reste (860 000 FCFA)

    // --- Constructeur ---
    public ResumeFonds(double renflouementInitial, double depenseAgapesAnnuelle, List<Map<String, Object>> detailsDepensesEvenements, double totalDepenses, double soldeActuel) {
        this.renflouementInitial = renflouementInitial;
        this.depenseAgapesAnnuelle = depenseAgapesAnnuelle;
        this.detailsDepensesEvenements = detailsDepensesEvenements;
        this.totalDepenses = totalDepenses;
        this.soldeActuel = soldeActuel;
    }

    // --- Getters (Nécessaires pour la conversion JSON) ---
    public double getRenflouementInitial() {
        return renflouementInitial;
    }

    public double getDepenseAgapesAnnuelle() {
        return depenseAgapesAnnuelle;
    }

    public List<Map<String, Object>> getDetailsDepensesEvenements() {
        return detailsDepensesEvenements;
    }

    public double getTotalDepenses() {
        return totalDepenses;
    }

    public double getSoldeActuel() {
        return soldeActuel;
    }
    
    // Les Setters ne sont pas nécessaires car l'objet est créé une fois dans le Service
}