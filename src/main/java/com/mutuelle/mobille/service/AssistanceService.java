package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.assistance.*;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.*;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssistanceService {

    private final TypeAssistanceRepository typeAssistanceRepository;
    private final AssistanceRepository assistanceRepository;
    private final SessionRepository sessionRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final AccountMutuelleRepository accountMutuelleRepository;

    // Récupérer tous les types d'assistance
    @Transactional(readOnly = true)
    public List<TypeAssistanceResponseDto> getAllTypeAssistances() {
        return typeAssistanceRepository.findAll().stream()
                .map(this::mapToTypeAssistanceResponseDto)
                .collect(Collectors.toList());
    }

    // Créer un nouveau type d'assistance
    public TypeAssistanceResponseDto createTypeAssistance(CreateTypeAssistanceDto dto) {
        TypeAssistance type = TypeAssistance.builder()
                .name(dto.name())
                .description(dto.description())
                .amount(dto.amount())
                .build();

        TypeAssistance saved = typeAssistanceRepository.save(type);
        return mapToTypeAssistanceResponseDto(saved);
    }

    // Mettre à jour un type d'assistance
    public TypeAssistanceResponseDto updateTypeAssistance(Long id, UpdateTypeAssistanceDto dto) {
        TypeAssistance type = typeAssistanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type d'assistance non trouvé avec l'ID : " + id));

        type.setName(dto.name());
        type.setDescription(dto.description());
        type.setAmount(dto.amount());

        TypeAssistance updated = typeAssistanceRepository.save(type);
        return mapToTypeAssistanceResponseDto(updated);
    }

    // Créer une assistance
    public Assistance createAssistance(CreateAssistanceDto dto) {
        // Récupération des entités nécessaires
        AccountMutuelle globalAccount = accountService.getMutuelleGlobalAccount();

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new EntityNotFoundException("Membre non trouvé avec l'ID : " + dto.memberId()));

        TypeAssistance typeAssistance = typeAssistanceRepository.findById(dto.typeAssistanceId())
                .orElseThrow(() -> new EntityNotFoundException("Type d'assistance non trouvé avec l'ID : " + dto.typeAssistanceId()));

        Session session = sessionRepository.findById(dto.sessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session non trouvée avec l'ID : " + dto.sessionId()));

        BigDecimal requiredAmount = typeAssistance.getAmount();

        // Vérification du solde de solidarité du compte global
        if (globalAccount.getSolidarityAmount().compareTo(requiredAmount) < 0) {
            throw new IllegalStateException("La mutuelle ne dispose pas d'assez de fonds pour cette assistance. Solde disponible : "
                    + globalAccount.getSolidarityAmount() + ", requis : " + requiredAmount);
        }

        // Débit du compte global de la mutuelle
        globalAccount.setSolidarityAmount(globalAccount.getSolidarityAmount().subtract(requiredAmount));
        // Pas besoin de save ici si AccountMutuelle est géré par cascade ou si le service le fait, sinon ajouter un save si nécessaire
        accountMutuelleRepository.save(globalAccount);

        // Créer la transaction associée (type ASSISTANCE)
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.ASSISTANCE)
                .amount(requiredAmount)
                .description("Demande d'assistance : " + typeAssistance.getName())
                .accountMember(member.getAccountMember())
                .session(session)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Créditer éventuellement le compte du membre ? (selon votre logique métier)
        // Si l'assistance verse de l'argent au membre, il faut créditer son compte ici.
        // Exemple possible (à adapter) :
        // member.getAccountMember().setBalance(member.getAccountMember().getBalance().add(requiredAmount));

        // Créer l'assistance
        Assistance assistance = Assistance.builder()
                .description(dto.description())
                .typeAssistance(typeAssistance)
                .transaction(savedTransaction)
                .member(member)
                .session(session)
                .build();

        return assistanceRepository.save(assistance);
    }

    // Méthode utilitaire pour mapper TypeAssistance → DTO
    private TypeAssistanceResponseDto mapToTypeAssistanceResponseDto(TypeAssistance type) {
        return new TypeAssistanceResponseDto(
                type.getId(),
                type.getName(),
                type.getAmount()
        );
    }

    /**
     * Calcule le montant total des assistances validées/accordées pour une session donnée.
     * (on peut filtrer sur un statut si tu en ajoutes un plus tard : APPROVED, PAID, etc.)
     */
    public BigDecimal getTotalAssistanceAmountForSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session non trouvée : " + sessionId));

        return transactionRepository.sumAssistanceAmountBySessionId(sessionId);
    }

    public Long countTotalAssistanceForSession(Long sessionId){
        return  transactionRepository.countBySessionId(sessionId);
    }
}