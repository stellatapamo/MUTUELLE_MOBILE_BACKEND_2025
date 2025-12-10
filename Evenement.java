package com.example.demo; 

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

// L'Entité Evenement représente une dépense de la tontine
@Entity 
public class Evenement {

    // Clé primaire auto-générée
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;         // Ex: "Mariage", "Décès Conjoint"
    private double montant;      // Le montant de la dépense
    private String beneficiaire; // Le membre bénéficiaire
    private LocalDate date;      // La date de la dépense

    // Constructeur vide nécessaire pour JPA
    public Evenement() {
    }

    // Constructeur utile
    public Evenement(String type, double montant, String beneficiaire, LocalDate date) {
        this.type = type;
        this.montant = montant;
        this.beneficiaire = beneficiaire;
        this.date = date;
    }

    // --- Getters et Setters (indispensables) ---
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}