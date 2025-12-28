package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.assistance.*;
import com.mutuelle.mobille.models.Assistance;
import com.mutuelle.mobille.service.AssistanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assistances")
@RequiredArgsConstructor
@Tag(name = "Gestion des Assistances")
public class AssistanceController {

    private final AssistanceService assistanceService;


    @GetMapping("/types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer la liste de tous les types d'assistance")
    public ResponseEntity<ApiResponseDto<List<TypeAssistanceResponseDto>>> getAllTypes() {
        List<TypeAssistanceResponseDto> types = assistanceService.getAllTypeAssistances();
        return ResponseEntity.ok(ApiResponseDto.ok(types, "Liste des types d'assistance récupérée avec succès"));
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un nouveau type d'assistance")
    public ResponseEntity<ApiResponseDto<TypeAssistanceResponseDto>> createType(
            @Valid @RequestBody CreateTypeAssistanceDto request) {

        TypeAssistanceResponseDto created = assistanceService.createTypeAssistance(request);
        return ResponseEntity.ok(ApiResponseDto.ok(created, "Type d'assistance créé avec succès"));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un type d'assistance")
    public ResponseEntity<ApiResponseDto<TypeAssistanceResponseDto>> updateType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTypeAssistanceDto request) {

        TypeAssistanceResponseDto updated = assistanceService.updateTypeAssistance(id, request);
        return ResponseEntity.ok(ApiResponseDto.ok(updated, "Type d'assistance mis à jour avec succès"));
    }

    // === Gestion des assistances (demandes) ===

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer une nouvelle demande d'assistance")
    public ResponseEntity<ApiResponseDto<Assistance>> createAssistance(
            @Valid @RequestBody CreateAssistanceDto request) {

        Assistance created = assistanceService.createAssistance(request);
        return ResponseEntity.ok(ApiResponseDto.ok(created, "Demande d'assistance créée avec succès"));
    }
}