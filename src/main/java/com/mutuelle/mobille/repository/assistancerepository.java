package mutuelle.back.assistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistanceRepository extends JpaRepository<Assistance, Long> {
    
    // Trouver par exercice
    List<Assistance> findByExerciceId(Long exerciceId);
    
    // Trouver par type et statut
    List<Assistance> findByTypeAssistanceIdAndStatut(Long typeAssistanceId, Assistance.StatutAssistance statut);
    
    // Trouver les assistances réutilisées
    List<Assistance> findByEstReutilisationTrueAndAssistanceSourceId(Long sourceId);
    
    // Trouver par demandeur
    List<Assistance> findByDemandeurId(Long membreId);
    
    // Statistiques d'un type d'assistance
    @Query("SELECT COUNT(a), COALESCE(SUM(a.montant), 0) " +
           "FROM Assistance a WHERE a.typeAssistance.id = :typeId")
    Object[] getStatistiquesType(@Param("typeId") Long typeId);
}