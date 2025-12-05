package com.mutuelle.mobille.dto.member;

import java.math.BigDecimal;

public record MemberProfileDTO(

        Long memberId,
        String fullName,
        String phone,
        String email,
        String avatar,
        boolean isActive,


        BigDecimal totalBorrowed,   // montant emprunté
        int pendingPaymentsCount    // nombre de paiements en attente (optionnel)

) {}