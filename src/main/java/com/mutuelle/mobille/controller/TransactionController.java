package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.transaction.EmpruntRequestDTO;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour la gestion des transactions financières (emprunts, remboursements)
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Effectuer un emprunt pour un membre
     */
    @PostMapping("/emprunt")
    @Operation(summary = "Effectuer un emprunt")
    public ResponseEntity<ApiResponseDto<Transaction>> emprunter(
            @Valid @RequestBody EmpruntRequestDTO request
    ) {

        Transaction transaction = transactionService.emprunter(
                request.getMemberId(),
                request.getAmount()
        );

        return ResponseEntity.ok(
                ApiResponseDto.ok(transaction, "Emprunt effectué avec succès")
        );
    }
}
