package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.auth.LoginRequestDto;
import com.mutuelle.mobille.dto.auth.LoginResponseDto;
import com.mutuelle.mobille.dto.auth.RefreshRequestDto;
import com.mutuelle.mobille.dto.auth.TokenResponseDto;
import com.mutuelle.mobille.dto.member.MemberRegisterDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.service.AuthService;
import com.mutuelle.mobille.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members")
public class MembersController {

    private final MemberService memberService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les membres actifs avec filtres optionnels")
    public ResponseEntity<ApiResponseDto<List<MemberResponseDTO>>> getAllMembers(
            // Filtres optionnels
            @RequestParam(required = false) String search,                          // recherche sur prénom, nom ou téléphone
            @RequestParam(required = false) Boolean active,                         // true/false/null (null = tous)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdBefore,

            // Pagination et tri manuels (comme ton exemple transactions)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,          // limite raisonnable
            @RequestParam(defaultValue = "lastname,asc") String sort) {

        // Préparation du tri
        String[] sortParams = sort.split(",");
        Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sortBy = Sort.by(sortDirection, sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortBy);

        // Appel au service
        Page<MemberResponseDTO> resultPage = memberService.getMembersFiltered(
                search, active, createdAfter, createdBefore, pageable);

        // Construction de la réponse paginée (style identique à ton exemple)
        ApiResponseDto<List<MemberResponseDTO>> response = ApiResponseDto.okPaged(
                resultPage.getContent(),
                resultPage.isEmpty() ? "Aucun membre trouvé" : "Liste des membres récupérée avec succès",
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.getNumber(),
                resultPage.getSize()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouveau membre - crée automatiquement : Member + Account + AuthUser")
    public ResponseEntity<ApiResponseDto<MemberResponseDTO>> register(@Valid @RequestBody MemberRegisterDTO request) { 
        MemberResponseDTO response = memberService.registerMember(request);
        return ResponseEntity.status(201)
                .body(ApiResponseDto.created(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Récupérer son propre profil + solde du compte")
    public ResponseEntity<ApiResponseDto<MemberResponseDTO>> getMyProfile() {
        MemberResponseDTO profile = memberService.getCurrentMemberProfile();
        return ResponseEntity.ok(ApiResponseDto.ok(profile, "Profil récupéré avec succès"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userRefId")
    @Operation(summary = "Voir le profil d'un membre (admin ou soi-même)")
    public ResponseEntity<ApiResponseDto<MemberResponseDTO>> getMemberById(@PathVariable Long id) {
        MemberResponseDTO member = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponseDto.ok(member, "Membre trouvé"));
    }

    @PatchMapping("/me/avatar")
    @Operation(summary = "Mettre à jour son avatar")
    public ResponseEntity<ApiResponseDto<String>> updateAvatar(@RequestParam String avatarUrl) {
        memberService.updateAvatar(avatarUrl);
        return ResponseEntity.ok(ApiResponseDto.ok(avatarUrl, "Avatar mis à jour"));
    }
}