package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.transaction.epargne.TransactionEpargneDto;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.mapper.transactionEpargne.DtoMapper;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.service.EpargneService;
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
import java.util.List;

@RestController
@RequestMapping("/api/epargne")
@RequiredArgsConstructor
@Tag(name = "Épargne", description = "Gestion des transactions d'épargne")
public class EpargneController {

    private final EpargneService epargneService;
    private final MemberRepository memberRepository;
    private final SessionRepository sessionRepository;

    // ========================================================================================
    // CREER UNE TRANSACTION D'EPARGNE
    // ========================================================================================
    @PostMapping
    @Operation(
            summary = "Créer une transaction d'épargne",
            description = "Permet de créer une transaction d'épargne (CREDIT ou DEBIT) pour un membre."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transaction créée avec succès",
            content = @Content(schema = @Schema(implementation = Transaction.class))
    )
    @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    public ResponseEntity<ApiResponseDto<TransactionEpargneDto>> transactionEpargne(
            @Parameter(description = "ID du membre concerné", example = "1")
            @RequestParam Long memberId,

            @Parameter(description = "ID de la session", example = "3")
            @RequestParam Long sessionId,

            @Parameter(description = "Montant de l'épargne", example = "5000")
            @RequestParam BigDecimal amount,

            @Parameter(description = "Direction de la transaction : CREDIT ou DEBIT", example = "CREDIT")
            @RequestParam String transactionDirection
    ) {

        // Vérification membre
        if (!memberRepository.existsById(memberId)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseDto.error("Le membre avec l'id " + memberId + " n'existe pas")
            );
        }

        // Vérification session
        if (!sessionRepository.existsById(sessionId)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseDto.error("La session avec l'id " + sessionId + " n'existe pas")
            );
        }

        // Vérification direction
        TransactionDirection direction;
        try {
            direction = TransactionDirection.valueOf(transactionDirection.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseDto.error("TransactionDirection invalide : utilisez CREDIT ou DEBIT")
            );
        }

        Transaction transaction =
                epargneService.processEpargne(memberId, sessionId, amount, direction);

        TransactionEpargneDto dto = DtoMapper.toTransactionDto(transaction);

        return ResponseEntity.ok(
                ApiResponseDto.ok(dto, "Transaction d'épargne effectuée avec succès")
        );


    }


}
