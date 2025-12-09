package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.service.SolidariteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solidarite")
public class SolidariteController {

    private final SolidariteService solidariteService;

    // Injection du service
    public SolidariteController(SolidariteService solidariteService) {
        this.solidariteService = solidariteService;
    }

    // Classe interne pour recevoir les données de paiement
    public static class PaiementRequest {
        private Long membreId;
        private BigDecimal montant;
        private Long sessionId;

        // Getters et Setters
        public Long getMembreId() {
            return membreId;
        }

        public void setMembreId(Long membreId) {
            this.membreId = membreId;
        }

        public BigDecimal getMontant() {
            return montant;
        }

        public void setMontant(BigDecimal montant) {
            this.montant = montant;
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }
    }

    // endpoint pour enregistrer un paiement de solidarité

    /**
     * Enregistrer un paiement de solidarité
     * POST /api/solidarite/payer
     */
    @PostMapping("/payer")
    public ResponseEntity<Map<String, Object>> enregistrerPaiement(@RequestBody PaiementRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Appeler le service pour enregistrer le paiement
            Transaction transaction = solidariteService.enregistrerPaiement(
                    request.getMembreId(),
                    request.getMontant(),
                    request.getSessionId());

            // Construire la réponse de succès
            response.put("success", true);
            response.put("message", "Paiement de solidarité enregistré avec succès");
            response.put("transaction", transaction);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // Erreur de validation (montant invalide)
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (RuntimeException e) {
            // Erreur (membre ou session introuvable)
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // endpoint pour récupérer l'historique des paiements de solidarité d'un membre

    /**
     * Récupérer l'historique des paiements de solidarité d'un membre
     * GET /api/solidarite/membre/{id}/historique
     */
    @GetMapping("/membre/{id}/historique")
    public ResponseEntity<Map<String, Object>> getHistoriquePaiements(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Récupérer les transactions
            List<Transaction> transactions = solidariteService.getHistoriquePaiements(id);

            // Calculer le total payé
            BigDecimal totalPaye = solidariteService.calculerTotalPaye(id);

            // Construire la réponse
            response.put("success", true);
            response.put("membreId", id);
            response.put("transactions", transactions);
            response.put("totalPaye", totalPaye);
            response.put("nombrePaiements", transactions.size());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // endpoints pour recuperer le total payé en solidarité par un membre

    /**
     * Récupérer le total payé en solidarité par un membre
     * GET /api/solidarite/membre/{id}/total
     */
    @GetMapping("/membre/{id}/total")
    public ResponseEntity<Map<String, Object>> getTotalPaye(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();

        try {
            BigDecimal totalPaye = solidariteService.calculerTotalPaye(id);

            response.put("success", true);
            response.put("membreId", id);
            response.put("totalPaye", totalPaye);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}