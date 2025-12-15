package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "interets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Borne inférieure de l'épargne pour le calcul de l'intérêt
    @Column(name = "borne_inferieure", precision = 12, scale = 2)
    private BigDecimal borneInferieure;

    // Borne supérieure de l'épargne pour le calcul de l'intérêt
    @Column(name = "borne_superieure", precision = 12, scale = 2)
    private BigDecimal borneSuperieure;

    // Taux d'intérêt applicable dans cette tranche
    @Column(name = "taux_interet", precision = 5, scale = 2)
    private BigDecimal tauxInteret;

    // Description de l'intérêt, pour faciliter l'affichage ou les logs
    @Column(name = "description")
    private String description;

    // Si nécessaire, pour pouvez ajouter une logique pour calculer l'intérêt basé sur l'épargne
    public BigDecimal calculerInteret(BigDecimal montant) {
        if (montant.compareTo(borneInferieure) >= 0 && montant.compareTo(borneSuperieure) <= 0) {
            return montant.multiply(tauxInteret);
        }
        return BigDecimal.ZERO;
    }
}
