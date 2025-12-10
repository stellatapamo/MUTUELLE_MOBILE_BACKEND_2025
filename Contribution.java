package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom du membre qui a effectué le paiement
    private String nomMembre;

    // Montant payé par ce membre pour une transaction donnée
    private double montantPaye;

    // Date du paiement
    private LocalDate datePaiement;

    // L'exercice ou l'année fiscale à laquelle cette contribution s'applique
    private int anneeExercice;

    // --- Constructeur vide (requis par JPA) ---
    public Contribution() {}

    // --- Constructeur utile ---
    public Contribution(String nomMembre, double montantPaye, LocalDate datePaiement, int anneeExercice) {
        this.nomMembre = nomMembre;
        this.montantPaye = montantPaye;
        this.datePaiement = datePaiement;
        this.anneeExercice = anneeExercice;
    }

    // --- Getters et Setters (nécessaires pour l'accès aux données) ---
    // (Ajoutez tous les getters et setters ici, comme vous l'avez fait pour Evenement.java)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomMembre() {
        return nomMembre;
    }

    public void setNomMembre(String nomMembre) {
        this.nomMembre = nomMembre;
    }

    public double getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(double montantPaye) {
        this.montantPaye = montantPaye;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public int getAnneeExercice() {
        return anneeExercice;
    }

    public void setAnneeExercice(int anneeExercice) {
        this.anneeExercice = anneeExercice;
    }
}