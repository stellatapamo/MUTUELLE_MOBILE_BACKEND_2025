package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.config.ConfigMutuelleRequestDto;
import com.mutuelle.mobille.dto.config.ConfigMutuelleResponseDto;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.service.AuthService;
import com.mutuelle.mobille.service.MutuelleConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Configuration Mutuelle", description = "Gestion des paramètres globaux de la mutuelle")
@RequiredArgsConstructor
public class ConfigController {

    private final MutuelleConfigService configService;
    private final AuthService authService;


    /**
     * Récupérer la configuration actuelle
     */
    @GetMapping("/current")
    @Operation(summary = "Obtenir la configuration actuelle de la mutuelle")
    public ResponseEntity<ApiResponseDto<ConfigMutuelleResponseDto>> getCurrentConfig() {
        MutuelleConfig config = configService.getCurrentConfig();
        return ResponseEntity.ok(ApiResponseDto.ok(
                new ConfigMutuelleResponseDto(config),
                "Configuration récupérée avec succès"
        ));
    }

    /**
     * Mettre à jour la configuration globale
     */
    @PatchMapping("/current/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Mettre à jour partiellement la configuration de la mutuelle",
            description = "Seuls les champs fournis dans le body sont modifiés. Réservé aux administrateurs."
    )
    public ResponseEntity<ApiResponseDto<ConfigMutuelleResponseDto>> partialUpdateConfig(
            @Valid @RequestBody ConfigMutuelleRequestDto request ) {


        MutuelleConfig current = configService.getCurrentConfig();

        // Appliquer uniquement les champs présents (non null) dans la requête
        boolean hasChanges = false;

        if (request.getRegistrationFeeAmount() != null) {
            current.setRegistrationFeeAmount(request.getRegistrationFeeAmount());
            hasChanges = true;
        }

        if (request.getLoanInterestRatePercent() != null) {
            current.setLoanInterestRatePercent(request.getLoanInterestRatePercent());
            hasChanges = true;
        }

        // Ajoute ici tous les autres champs possibles de la même façon
        // if (request.getSomeOtherField() != null) {
        //     current.setSomeOtherField(request.getSomeOtherField());
        //     hasChanges = true;
        // }

        if (!hasChanges) {
            // Option A : renvoyer 200 OK sans rien faire
            return ResponseEntity.ok(ApiResponseDto.ok(
                    new ConfigMutuelleResponseDto(current),
                    "Aucune modification fournie → configuration inchangée"
            ));

            // Option B : renvoyer 400 Bad Request (plus strict)
            // throw new IllegalArgumentException("Aucun champ à mettre à jour");
        }

        AuthUser currentUser = authService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

        MutuelleConfig saved = configService.updateConfig(current, currentUser.getRole().getValue());

        return ResponseEntity.ok(ApiResponseDto.ok(
                new ConfigMutuelleResponseDto(saved),
                "Configuration mise à jour avec succès (mise à jour partielle)"
        ));
    }
}