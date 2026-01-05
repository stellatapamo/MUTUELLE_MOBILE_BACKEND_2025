package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.session.SessionRequestDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions (Périodes mensuelles ou trimestrielles dans un exercice)")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(summary = "Lister toutes les sessions (accessible à tous les utilisateurs authentifiés)")
    public ResponseEntity<ApiResponseDto<List<SessionResponseDTO>>> getAllSessions() {
        List<SessionResponseDTO> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(ApiResponseDto.ok(sessions, "Liste des sessions récupérée"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une session par ID (accessible à tous les utilisateurs authentifiés)")
    public ResponseEntity<ApiResponseDto<SessionResponseDTO>> getSessionById(@PathVariable Long id) {
        SessionResponseDTO session = sessionService.getSessionById(id);
        return ResponseEntity.ok(ApiResponseDto.ok(session, "Session trouvée"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une nouvelle session (administrateurs uniquement)")
    public ResponseEntity<ApiResponseDto<SessionResponseDTO>> createSession(@Valid @RequestBody SessionRequestDTO request) {
        SessionResponseDTO response = sessionService.createSession(request);
        return ResponseEntity.status(201).body(ApiResponseDto.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une session (administrateurs uniquement)")
    public ResponseEntity<ApiResponseDto<SessionResponseDTO>> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionRequestDTO request) {
        SessionResponseDTO response = sessionService.updateSession(id, request);
        return ResponseEntity.ok(ApiResponseDto.ok(response, "Session mise à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une session (administrateurs uniquement)")
    public ResponseEntity<ApiResponseDto<Void>> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Session supprimée avec succès"));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponseDto<SessionResponseDTO>> getCurrentSession() {
        Session session = sessionService.getCurrentSession();
        return ResponseEntity.ok(ApiResponseDto.ok(sessionService.mapToResponseDTO(session)));
    }
}