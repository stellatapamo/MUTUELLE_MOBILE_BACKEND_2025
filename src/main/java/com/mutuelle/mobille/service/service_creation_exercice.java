package mutuelle.back.exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ExerciceService {
    
    @Autowired
    private ExerciceRepository exerciceRepository;
    
    @Autowired
    private CaisseMutuelleRepository caisseMutuelleRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Transactional
    public Exercice creerExercice(String nom, LocalDate dateDebut) {
        // 1. Vérifier qu'aucun exercice n'est actif
        Optional<Exercice> exerciceActif = exerciceRepository.findByStatut(Exercice.StatutExercice.ACTIF);
        if (exerciceActif.isPresent()) {
            throw new IllegalStateException("Un exercice est déjà actif");
        }
        
        // 2. Créer le nouvel exercice
        Exercice nouvelExercice = new Exercice();
        nouvelExercice.setNom(nom);
        nouvelExercice.setDateDebut(dateDebut);
        nouvelExercice.setStatut(Exercice.StatutExercice.ACTIF);
        
        // 3. Initialiser la première session
        Session premiereSession = new Session();
        premiereSession.setNom("Session d'ouverture - " + nom);
        premiereSession.setDateSession(dateDebut);
        premiereSession.setExercice(nouvelExercice);
        premiereSession.setStatut(Session.StatutSession.ACTIVE);
        
        nouvelExercice.getSessions().add(premiereSession);
        
        // 4. Sauvegarder
        Exercice saved = exerciceRepository.save(nouvelExercice);
        sessionRepository.save(premiereSession);
        
        return saved;
    }
} @Autowired
    private SessionService sessionService;
    
    public Session ajouterSessionAExercice(Long exerciceId, String nomSession, LocalDate dateSession) {
        Exercice exercice = getExerciceById(exerciceId);
        
        Session nouvelleSession = new Session();
        nouvelleSession.setNom(nomSession);
        nouvelleSession.setDateSession(dateSession);
        nouvelleSession.setExercice(exercice);
        nouvelleSession.setStatut(Session.StatutSession.PLANIFIEE);
        
        exercice.getSessions().add(nouvelleSession);
        exerciceRepository.save(exercice);
        
        return sessionService.creerSession(nouvelleSession);
    }
    
    public List<Session> getSessionsByExercice(Long exerciceId) {
        Exercice exercice = getExerciceById(exerciceId);
        return sessionService.getSessionsByExercice(exercice);
    }
    
    // ========= COLLABORATION AVEC RENFLOUEMENT ( partie avec Kevine & Ulrich) =========
    
    @Autowired
    private RenflouementService renflouementService;
    
    public Double calculerRenflouementPourExercice(Long exerciceId) {
        Exercice exercice = getExerciceById(exerciceId);
        
        // 5% de l'épargne totale (selon diagramme)
        Double renflouementDu = exercice.getEpargneTotale() * 0.05;
        
        // Mettre à jour l'exercice
        exercice.setRenflouementDu(renflouementDu);
        exerciceRepository.save(exercice);
        
        return renflouementDu;
    }
    
    public void initierRenflouementPourMembres(Long exerciceId) {
        Exercice exercice = getExerciceById(exerciceId);
        
        // Pour chaque membre, créer une transaction renflouement
        
        renflouementService.creerRenflouementsPourExercice(exercice);
    }
    
    // ========= COLLABORATION AVEPR ÉPARGNE (partie avec Stella & Kevine) =========
    
    @Autowired
    private TransactionEpargneService transactionEpargneService;
    
    public Double calculerEpargneTotaleExercice(Long exerciceId) {
        Exercice exercice = getExerciceById(exerciceId);
        
        // Somme de toutes les transactions d'épargne liées aux sessions de cet exercice
        Double totalEpargne = 0.0;
        
        for (Session session : exercice.getSessions()) {
            totalEpargne += transactionEpargneService.getTotalEpargneSession(session.getId());
        }
        
        // Mettre à jour l'exercice
        exercice.setEpargneTotale(totalEpargne);
        exerciceRepository.save(exercice);
        
        return totalEpargne;
    }
    
    // ========= COLLABORATION AVEC EMPRUNT (partie avec Andy) =========
    
    @Autowired
    private TransactionEmpruntService empruntService;
    
    public Double calculerTotalEmpruntsExercice(Long exerciceId) {
        Exercice exercice = getExerciceById(exerciceId);
        
        // Somme de tous les emprunts pendant la période de l'exercice
        Double totalEmprunts = empruntService.getTotalEmpruntsByPeriod(
            exercice.getDateDebut(), 
            exercice.getDateFin() != null ? exercice.getDateFin() : LocalDate.now()
        );
        
        // Mettre à jour l'exercice
        exercice.setEmpruntTotal(totalEmprunts);
        exerciceRepository.save(exercice);
        
        return totalEmprunts;
    }
}