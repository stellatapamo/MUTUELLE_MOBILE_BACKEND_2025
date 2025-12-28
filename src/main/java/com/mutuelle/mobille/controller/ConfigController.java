package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.ConfigMutuelleRequestDto;
import com.mutuelle.mobille.dto.ConfigMutuelleResponseDto;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.service.MutuelleConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Configuration Mutuelle", description = "Gestion des paramètres globaux de la mutuelle")
public class ConfigController {

    private final MutuelleConfigService configService;

    public ConfigController(MutuelleConfigService configService) {
        this.configService = configService;
    }

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
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour les configurations globales de la mutuelle",
            description = "Réservé aux administrateurs")
    public ResponseEntity<ApiResponseDto<ConfigMutuelleResponseDto>> updateConfig(
            @Valid @RequestBody ConfigMutuelleRequestDto request,
            Authentication authentication) {

        MutuelleConfig currentConfig = configService.getCurrentConfig();

        // Préparer l'objet à mettre à jour
        MutuelleConfig updated = new MutuelleConfig();
        updated.setRegistrationFeeAmount(request.getRegistrationFeeAmount());
        updated.setLoanInterestRatePercent(request.getLoanInterestRatePercent());
        updated.setAgapeAmount(request.getAgapeAmount());

        String updatedBy = authentication != null && authentication.getName() != null
                ? authentication.getName()
                : "anonymous";

        MutuelleConfig saved = configService.updateConfig(updated, updatedBy);

        return ResponseEntity.ok(ApiResponseDto.ok(
                new ConfigMutuelleResponseDto(saved),
                "Configuration mise à jour avec succès"
        ));
    }
}