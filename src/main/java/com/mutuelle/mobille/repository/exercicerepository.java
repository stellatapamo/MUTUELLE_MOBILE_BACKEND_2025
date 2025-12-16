package mutuelle.back.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    
    // Trouver l'exercice actif
    Optional<Exercice> findByStatut(Exercice.StatutExercice statut);
    
    // Trouver par période
    List<Exercice> findByDateDebutBetween(LocalDate start, LocalDate end);
    
    // Vérifier si un exercice peut être clôturé
    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN true ELSE false END " +
           "FROM TransactionEpargne t WHERE t.date >= :dateDebut AND t.exercice.id = :exerciceId")
    boolean peutEtreCloture(@Param("exerciceId") Long exerciceId, @Param("dateDebut") LocalDate dateDebut);
    
    // Calculer les totaux pour un exercice
    @Query("SELECT NEW mutuelle.back.exercise.StatistiquesExercice( " +
           "COALESCE(SUM(te.montant), 0), " +
           "COALESCE(SUM(ts.montant), 0), " +
           "COALESCE(SUM(em.montantEmprunte), 0), " +
           "COALESCE(SUM(r.montant), 0) " +
           ") FROM Exercice e " +
           "LEFT JOIN e.sessions s " +
           "LEFT JOIN TransactionEpargne te ON te.session.id = s.id " +
           "LEFT JOIN TransactionSolidarite ts ON ts.session.id = s.id " +
           "LEFT JOIN TransactionEmprunt em ON em.membre.id IN " +
           "(SELECT m.id FROM Membre m WHERE m.dateInscription BETWEEN e.dateDebut AND e.dateFin) " +
           "LEFT JOIN TransactionRenflouement r ON r.exercice.id = e.id " +
           "WHERE e.id = :exerciceId")
    StatistiquesExercice calculerStatistiques(@Param("exerciceId") Long exerciceId);
}