<< 'EOF'
package mutuelle.back.exercice.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercices")
public class Exercice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin")
    private LocalDate dateFin;
    
    @Enumerated(EnumType.STRING)
    private StatutExercice statut = StatutExercice.ACTIF;
    
    // Relations
    @OneToMany(mappedBy = "exercice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Session> sessions = new ArrayList<>();
    
    @OneToMany(mappedBy = "exercice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assistance> assistances = new ArrayList<>();
    
    // Statistiques
    @Column(name = "epargne_totale")
    private Double epargneTotale = 0.0;
    
    @Column(name = "solidarite_totale")
    private Double solidariteTotale = 0.0;
    
    @Column(name = "emprunt_total")
    private Double empruntTotal = 0.0;
    
    @Column(name = "renflouement_du")
    private Double renflouementDu = 0.0;
    
    @Column(name = "renflouement_paye")
    private Double renflouementPaye = 0.0;
    
    // Constructeurs
    public Exercice() {}
    
    public Exercice(String nom, LocalDate dateDebut) {
        this.nom = nom;
        this.dateDebut = dateDebut;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    
    public StatutExercice getStatut() { return statut; }
    public void setStatut(StatutExercice statut) { this.statut = statut; }
    
    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }
    
    public List<Assistance> getAssistances() { return assistances; }
    public void setAssistances(List<Assistance> assistances) { this.assistances = assistances; }
    
    public Double getEpargneTotale() { return epargneTotale; }
    public void setEpargneTotale(Double epargneTotale) { this.epargneTotale = epargneTotale; }
    
    public Double getSolidariteTotale() { return solidariteTotale; }
    public void setSolidariteTotale(Double solidariteTotale) { this.solidariteTotale = solidariteTotale; }
    
    public Double getEmpruntTotal() { return empruntTotal; }
    public void setEmpruntTotal(Double empruntTotal) { this.empruntTotal = empruntTotal; }
    
    public Double getRenflouementDu() { return renflouementDu; }
    public void setRenflouementDu(Double renflouementDu) { this.renflouementDu = renflouementDu; }
    
    public Double getRenflouementPaye() { return renflouementPaye; }
    public void setRenflouementPaye(Double renflouementPaye) { this.renflouementPaye = renflouementPaye; }
    
    // Enum
    public enum StatutExercice {
        ACTIF, CLOTURE
    }
}
EOF
