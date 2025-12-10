package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Importation nécessaire pour List<Map<String, String>>

// Indique à Spring que cette classe est un composant de l'API REST
@RestController
// Définit le préfixe de toutes les routes de ce contrôleur
@RequestMapping("/api/contribution")
public class ContributionController {

    // Injection du service pour accéder à la logique métier
    @Autowired
    private RenflouementService renflouementService;

    // --- 1. Route pour enregistrer une nouvelle contribution (POST) ---
    // Exemple d'appel : POST http://localhost:8080/api/contribution
    @PostMapping
    public ResponseEntity<Contribution> enregistrerContribution(@RequestBody Contribution contribution) {
        
        Contribution nouvelleContribution = renflouementService.enregistrerContribution(contribution);
        
        // Retourne la contribution enregistrée avec le statut 201 CREATED
        return new ResponseEntity<>(nouvelleContribution, HttpStatus.CREATED);
    }

    // --- 2. Route pour récupérer toutes les contributions (GET) ---
    // Exemple d'appel : GET http://localhost:8080/api/contribution/toutes
    @GetMapping("/toutes")
    public ResponseEntity<List<Contribution>> getAllContributions() {
        
        List<Contribution> contributions = renflouementService.getAllContributions();
        
        // Retourne la liste avec le statut 200 OK
        return new ResponseEntity<>(contributions, HttpStatus.OK);
    }
    
    // --- 3. Route pour vérifier le statut de paiement d'un membre (GET) ---
    // Exemple : GET http://localhost:8080/api/contribution/statut?membre=Membre%20Alpha&annee=2025
    @GetMapping("/statut")
    public ResponseEntity<String> getStatutMembre(
            @RequestParam("membre") String nomMembre,
            @RequestParam("annee") int annee
    ) {
        boolean estEnRegle = renflouementService.verifierStatutMembre(nomMembre, annee);
        
        String statut = nomMembre + " (Année " + annee + ") est en règle : " + (estEnRegle ? "OUI" : "NON");
        
        return new ResponseEntity<>(statut, HttpStatus.OK);
    }
    
    // --- 4. Route pour afficher le statut de tous les membres (GET) ---
    // Exemple : GET http://localhost:8080/api/contribution/statuts/tous?annee=2025
    @GetMapping("/statuts/tous")
    public ResponseEntity<List<Map<String, String>>> getAllStatuts(
            @RequestParam("annee") int annee
    ) {
        // Délégation au Service pour calculer et renvoyer la liste des statuts
        List<Map<String, String>> statuts = renflouementService.getAllStatutsMembres(annee);
        
        return new ResponseEntity<>(statuts, HttpStatus.OK);
    }
}