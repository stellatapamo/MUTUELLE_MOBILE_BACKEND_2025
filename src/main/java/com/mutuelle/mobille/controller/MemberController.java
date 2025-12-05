package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.member.MemberRegisterDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Account")
public  class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    @Operation(summary = "Récupérer son propre profil + solde du compte")
    public ResponseEntity<ApiResponseDto<MemberResponseDTO>> getMyProfile() {
        MemberResponseDTO profile = memberService.getCurrentMemberProfile();
        return ResponseEntity.ok(ApiResponseDto.ok(profile, "Profil récupéré avec succès"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.refId")
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

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouveau membre → crée automatiquement : Member + Account + AuthUser")
    public ResponseEntity<ApiResponseDto<MemberResponseDTO>> registerMember(
            @Valid @RequestBody MemberRegisterDTO request) {

        MemberResponseDTO response = memberService.registerMember(request);
        return ResponseEntity.status(201)
                .body(ApiResponseDto.created(response));
    }
}