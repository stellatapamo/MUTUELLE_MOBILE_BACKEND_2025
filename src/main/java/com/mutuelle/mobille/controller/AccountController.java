package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.account.AccountMemberResponseDTO;
import com.mutuelle.mobille.dto.member.AccountMemberDTO;
import com.mutuelle.mobille.mapper.transactionEpargne.DtoMapper;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Comptes (Membres et Mutuelle)", description = "Gestion de la consultation des comptes membres et caisse mutuelle")
public class AccountController {

    private final AccountService accountService;

    // ───────────────────────────────────────────────────────────────
    // Comptes des membres
    // ───────────────────────────────────────────────────────────────

    @GetMapping("/members")
    @Operation(summary = "Lister tous les comptes des membres")
    public ResponseEntity<ApiResponseDto<List<AccountMemberDTO>>> getAllMemberAccounts() {
        List<AccountMember> accounts = accountService.getAllMemberAccounts();

        List<AccountMemberDTO> dtos = accounts.stream()
                .map(DtoMapper::toAccountMemberDto)
                .toList();

        return ResponseEntity.ok(
                ApiResponseDto.ok(dtos, "Liste de tous les comptes membres récupérée")
        );
    }

    @GetMapping("/members/{id}")
    @Operation(summary = "Récupérer le compte d'un membre spécifique par son ID")
    public ResponseEntity<ApiResponseDto<AccountMember>> getMemberAccountById(@PathVariable Long id) {
        AccountMember account = accountService.getMemberAccountById(id);
        return ResponseEntity.ok(
                ApiResponseDto.ok(account, "Compte du membre récupéré avec succès")
        );
    }

    @GetMapping("/members/by-member/{memberId}")
    @Operation(summary = "Récupérer le compte d'un membre à partir de son ID membre")
    public ResponseEntity<ApiResponseDto<AccountMember>> getMemberAccountByMemberId(@PathVariable Long memberId) {
        AccountMember account = accountService.getMemberAccountByMemberId(memberId);
        return ResponseEntity.ok(
                ApiResponseDto.ok(account, "Compte du membre récupéré avec succès")
        );
    }

    // ───────────────────────────────────────────────────────────────
    // Compte Mutuelle (caisse centrale)
    // ───────────────────────────────────────────────────────────────

    @GetMapping("/mutuelle")
    @Operation(summary = "Récupérer l'état de la caisse mutuelle (compte central)")
    public ResponseEntity<ApiResponseDto<AccountMutuelle>> getMutuelleAccount() {
        AccountMutuelle account = accountService.getMutuelleGlobalAccount();
        return ResponseEntity.ok(
                ApiResponseDto.ok(account, "État de la caisse mutuelle récupéré")
        );
    }
}