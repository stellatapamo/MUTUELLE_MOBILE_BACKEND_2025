package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.transaction.EmpruntRequestDTO;
import com.mutuelle.mobille.dto.transaction.RemboursementRequestDTO;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emprunt-remboursement")
@RequiredArgsConstructor
@Tag(name = "mprunt-remboursement")
public class TransactionController {

    private final TransactionService transactionService;

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

    @PostMapping("/remboursement")
    @Operation(summary = "Effectuer un remboursement d'emprunt")
    public ResponseEntity<ApiResponseDto<Transaction>> rembourser(
            @Valid @RequestBody RemboursementRequestDTO request
    ) {

        Transaction transaction = transactionService.rembourser(
                request.getMemberId(),
                request.getAmount()
        );

        return ResponseEntity.ok(
                ApiResponseDto.ok(transaction, "Remboursement effectué avec succès")
        );
    }
}
