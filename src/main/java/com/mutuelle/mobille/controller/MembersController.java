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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members")
public class MembersController {

    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouveau membre - crée automatiquement : Member + Account + AuthUser")
    public ResponseEntity<ApiResponseDto<MemberResponseDTO>> register(@Valid @RequestBody MemberRegisterDTO request) {
        System.out.println("request.getEmail()request.getEmail()request.getEmail() : " + request.getEmail());
        System.out.println("request.toString()  :  ");
        System.out.println( request );
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

}