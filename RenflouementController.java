package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @RestController marque la classe comme un contrôleur REST
@RestController
// Définit le préfixe de base pour toutes les routes de ce contrôleur
@RequestMapping("/api/renflouement")
public class RenflouementController {

    // Injection du service contenant la logique métier
    @Autowired
    private RenflouementService renflouementService;

    // --- 1. Route pour enregistrer une nouvelle dépense (POST) ---
    // Exemple : POST http://localhost:8080/api/renflouement/depense
    @PostMapping("/depense")
    public ResponseEntity<Evenement> enregistrerDepense(@RequestBody Evenement evenement) {
        Evenement nouvelleDepense = renflouementService.enregistrerDepense(evenement);
        return new ResponseEntity<>(nouvelleDepense, HttpStatus.CREATED);
    }

    // --- 2. Route pour récupérer toutes les dépenses (GET) ---
    // Exemple : GET http://localhost:8080/api/renflouement/depenses
    @GetMapping("/depenses")
    public ResponseEntity<List<Evenement>> getAllDepenses() {
        List<Evenement> depenses = renflouementService.getAllDepenses();
        return new ResponseEntity<>(depenses, HttpStatus.OK);
    }
    
    // --- 3. Route pour afficher le Résumé du Fonds (GET) ---
    // Remplace l'ancienne route /solde par un résumé détaillé.
    // Exemple : GET http://localhost:8080/api/renflouement/resume?membres=10
    @GetMapping("/resume") 
    public ResponseEntity<ResumeFonds> getResumeFonds(
            @RequestParam(name = "membres", defaultValue = "10") int nombreMembres
    ) {
        // La méthode a changé de nom dans le Service
        ResumeFonds resume = renflouementService.calculerResumeFonds(nombreMembres);
        
        return new ResponseEntity<>(resume, HttpStatus.OK);
    }
    
    // --- 4. Route pour supprimer un événement par ID (DELETE) ---
    // Exemple : DELETE http://localhost:8080/api/renflouement/depense/2
    @DeleteMapping("/depense/{id}")
    public ResponseEntity<Void> supprimerEvenement(@PathVariable Long id) {
        renflouementService.supprimerEvenement(id);
        
        // Statut 204 NO CONTENT: succès sans corps de réponse
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}