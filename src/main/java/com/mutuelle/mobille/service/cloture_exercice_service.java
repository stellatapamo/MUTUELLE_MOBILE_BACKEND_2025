@Service
public class ClotureExerciceService {
    
    @Autowired
    private ExerciceRepository exerciceRepository;
    
    @Autowired
    private ExerciceService exerciceService;
    
    @Autowired
    private CaisseMutuelleService caisseMutuelleService;
    
    @Autowired
    private CaisseMembreService caisseMembreService;
    
    @Autowired
    private MembreService membreService;
    
    @Transactional
    public Exercice cloturerExercice(Long exerciceId, LocalDate dateFin) {
        // 1. Récupérer l'exercice
        Exercice exercice = exerciceRepository.findById(exerciceId)
            .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));
            
        if (exercice.getStatut() == Exercice.StatutExercice.CLOTURE) {
            throw new IllegalStateException("L'exercice est déjà clôturé");
        }
        
        // 2. Vérifier que l'exercice à clôturer est actif
        if (exercice.getStatut() != Exercice.StatutExercice.ACTIF) {
            throw new IllegalStateException("Seul un exercice ACTIF peut être clôturé");
        }
        
        // 3. Calculer toutes les statistiques (collaboration avec autres modules)
        calculerStatistiquesExercice(exercice);
        
        // 4. Transférer les données aux caisses (collaboration avec module Caisse)
        transfererDonneesVersCaisses(exercice);
        
        // 5. Mettre à jour l'exercice
        exercice.setDateFin(dateFin);
        exercice.setStatut(Exercice.StatutExercice.CLOTURE);
        
        // 6. Notifier les autres modules (événements)
        notifierModulesCloture(exercice);
        
        // 7. Sauvegarder
        return exerciceRepository.save(exercice);
    }
    
    private void calculerStatistiquesExercice(Exercice exercice) {
        // Collaboration avec module Épargne
        Double epargneTotale = exerciceService.calculerEpargneTotaleExercice(exercice.getId());
        exercice.setEpargneTotale(epargneTotale);
        
        // Collaboration avec module Emprunt
        Double totalEmprunts = exerciceService.calculerTotalEmpruntsExercice(exercice.getId());
        exercice.setEmpruntTotal(totalEmprunts);
        
        // Collaboration avec module Renflouement
        Double renflouementDu = exerciceService.calculerRenflouementPourExercice(exercice.getId());
        exercice.setRenflouementDu(renflouementDu);
        
        // Note: SolidaritéTotale sera calculée par le module Solidarité (Darelle)
    }
    
    private void transfererDonneesVersCaisses(Exercice exercice) {
        // 1. Mettre à jour la Caisse Mutuelle
        CaisseMutuelle caisseMutuelle = caisseMutuelleService.getCaisseMutuelle();
        caisseMutuelle.setEpargneTotal(caisseMutuelle.getEpargneTotal() + exercice.getEpargneTotale());
        caisseMutuelle.setRenflouementImpaye(caisseMutuelle.getRenflouementImpaye() + exercice.getRenflouementDu());
        caisseMutuelleService.save(caisseMutuelle);
        
        // 2. Mettre à jour les Caisses Membres
        List<Membre> membresActifs = membreService.getMembresActifs();
        
        for (Membre membre : membresActifs) {
            CaisseMembre caisseMembre = caisseMembreService.getCaisseMembreByMembre(membre);
            
            // Calculer l'épargne individuelle du membre pour cet exercice
            Double epargneMembre = calculerEpargneMembreExercice(membre, exercice);
            
            // Ajouter à la caisse du membre
            caisseMembre.setEpargne(caisseMembre.getEpargne() + epargneMembre);
            
            // Calculer renflouement dû par le membre (5% de son épargne)
            Double renflouementMembre = epargneMembre * 0.05;
            caisseMembre.setRenflouementImpaye(caisseMembre.getRenflouementImpaye() + renflouementMembre);
            
            caisseMembreService.save(caisseMembre);
        }
    }
    
    private void notifierModulesCloture(Exercice exercice) {
        // Cette méthode serait utilisée avec des événements Spring
        // Pour notifier les autres modules de la clôture
        // Pour l'instant, c'est un placeholder
        
        // Exemple d'événements à émettre :
        // - ExerciceClosedEvent
        // - TransfertDataEvent
        // - NouvelExerciceEvent
        
        // Les autres modules écouteront ces événements
    }
}