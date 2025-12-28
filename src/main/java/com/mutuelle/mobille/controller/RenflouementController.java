package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.repository.ExerciceRepository;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.repository.MutuelleConfigRepository; // Ajouté
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.service.RenflouementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/renflouement")
@RequiredArgsConstructor
@Tag(
        name = "Renflouement",
        description = "Gestion du renflouement annuel et du paiement par les membres"
)
public class RenflouementController {

    private final RenflouementService renflouementService;
    private final ExerciceRepository exerciceRepository;
    private final MemberRepository memberRepository;
    private final SessionRepository sessionRepository;
    private final MutuelleConfigRepository mutuelleConfigRepository; // Ajouté

    // =========================================================================
    // 1️⃣ CLÔTURE DU RENFLOUEMENT (FIN D'EXERCICE)
    // =========================================================================
    @PostMapping("/cloture")
    @Operation(
            summary = "Clôturer le renflouement annuel",
            description = """
                    Calcule le renflouement annuel selon la formule :
                    (Somme des assistances de l'année + agape * 12) / nombre de membres à jour.
                    
                    L'agape est récupérée depuis la configuration de la mutuelle (MutuelleConfig).
                    
                    Le montant est ajouté comme dette de renflouement
                    pour chaque membre à jour de son inscription.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Renflouement appliqué avec succès",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    @ApiResponse(responseCode = "404", description = "Exercice ou configuration introuvable")
    public ResponseEntity<ApiResponseDto<Void>> cloturerRenflouement(
            @Parameter(
                    description = "ID de l'exercice à clôturer",
                    example = "1",
                    required = true
            )
            @RequestParam Long exerciceId
    ) {

        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice introuvable"));

        if (LocalDateTime.now().isBefore(exercice.getEndDate())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("La clôture du renflouement ne peut se faire qu'à la fin de l'exercice"));
        }

        // Récupérer la dernière configuration automatiquement (recommandé)
        MutuelleConfig config = mutuelleConfigRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new RuntimeException("Configuration de la mutuelle introuvable"));

        BigDecimal agape = config.getAgapeAmount();

        renflouementService.processRenflouement(exercice, agape);

        return ResponseEntity.ok(
                ApiResponseDto.ok(null, "Renflouement appliqué avec succès")
        );
    }

    // =========================================================================
    // 2️⃣ PAIEMENT DU RENFLOUEMENT
    // =========================================================================
    @PostMapping("/payer")
    @Operation(
            summary = "Payer le renflouement",
            description = """
                    Permet à un membre de payer tout ou partie de sa dette
                    de renflouement.
                    
                    Conditions :
                    - Le membre doit être à jour de son inscription
                    - Le montant ne peut pas dépasser la dette restante
                    - Une transaction RENFOULEMENT est créée
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Renflouement payé avec succès",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Données invalides ou dette inexistante")
    @ApiResponse(responseCode = "404", description = "Membre ou session introuvable")
    public ResponseEntity<ApiResponseDto<Void>> payerRenflouement(
            @Parameter(
                    description = "ID du membre",
                    example = "5",
                    required = true
            )
            @RequestParam Long memberId,

            @Parameter(
                    description = "ID de la session courante",
                    example = "3",
                    required = true
            )
            @RequestParam Long sessionId,

            @Parameter(
                    description = "Montant à payer",
                    example = "500",
                    required = true
            )
            @RequestParam BigDecimal amount
    ) {

        if (!memberRepository.existsById(memberId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Membre introuvable"));
        }

        if (!sessionRepository.existsById(sessionId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Session introuvable"));
        }

        renflouementService.payRenflouement(memberId, sessionId, amount);

        return ResponseEntity.ok(
                ApiResponseDto.ok(null, "Renflouement payé avec succès")
        );
    }
}