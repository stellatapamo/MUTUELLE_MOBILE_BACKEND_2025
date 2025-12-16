@Service
public class ClotureExerciceService {
    
    @Autowired
    private ExerciceRepository exerciceRepository;
    
    @Autowired
    private CaisseMutuelleRepository caisseMutuelleRepository;
    
    @Autowired
    private CaisseMembreRepository caisseMembreRepository;
    
    @Autowired
    private MembreRepository membreRepository;
    
    @Transactional
    public Exercice cloturerExercice(Long exerciceId, LocalDate dateFin) {
        // 1. Récupérer l'exercice actif
        Exercice exercice = exerciceRepository.findById(exerciceId)
            .orElseThrow(() -> new NotFoundException("Exercice non trouvé"));
            
        if (exercice.getStatut() == Exercice.StatutExercice.CLOTURE) {
            throw new IllegalStateException("L'exercice est déjà clôturé");
        }
        
        // 2. Calculer les soldes finaux
        CaisseMutuelle caisseMutuelle = caisseMutuelleRepository.findById(1L)
            .orElseGet(() -> creerCaisseMutuelle());
            
        // 3. Calculer le renflouement dû (5% de l'épargne totale selon diagramme)
        Double epargneTotale = calculerEpargneTotale(exercice);
        Double renflouementDu = epargneTotale * 0.05; // 5%
        
        // 4. Mettre à jour les caisses membres
        List<Membre> membres = membreRepository.findByDateInscriptionBetween(
            exercice.getDateDebut(), dateFin);
            
        for (Membre membre : membres) {
            CaisseMembre caisseMembre = caisseMembreRepository.findByMembreId(membre.getId())
                .orElseGet(() -> creerCaisseMembre(membre));
                
            // Transférer l'épargne
            Double epargneMembre = calculerEpargneMembre(membre, exercice);
            caisseMembre.setEpargne(caisseMembre.getEpargne() + epargneMembre);
            
            // Calculer renflouement impayé
            Double renflouementMembreDu = epargneMembre * 0.05;
            caisseMembre.setRenflouementImpaye(caisseMembre.getRenflouementImpaye() + renflouementMembreDu);
            
            caisseMembreRepository.save(caisseMembre);
        }
        
        // 5. Mettre à jour la caisse mutuelle
        caisseMutuelle.setEpargneTotal(caisseMutuelle.getEpargneTotal() + epargneTotale);
        caisseMutuelle.setRenflouementImpaye(caisseMutuelle.getRenflouementImpaye() + renflouementDu);
        caisseMutuelleRepository.save(caisseMutuelle);
        
        // 6. Marquer l'exercice comme clôturé
        exercice.setDateFin(dateFin);
        exercice.setStatut(Exercice.StatutExercice.CLOTURE);
        exercice.setEpargneTotale(epargneTotale);
        exercice.setRenflouementDu(renflouementDu);
        
        // 7. Créer le rapport de clôture
        creerRapportCloture(exercice, epargneTotale, renflouementDu);
        
        return exerciceRepository.save(exercice);
    }
    
    private Double calculerEpargneTotale(Exercice exercice) {
        // Implémentation réelle basée sur TransactionEpargne
        return exerciceRepository.calculerStatistiques(exercice.getId()).getEpargneTotale();
    }
}