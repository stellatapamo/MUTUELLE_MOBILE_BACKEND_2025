// 2. MemberResponseDTO.java
// Retourné après inscription, login, ou consultation de profil
package com.mutuelle.mobille.dto.member;

import com.mutuelle.mobille.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberResponseDTO(
        Long authId,
        Long id,
        String firstname,
        String lastname,
        String phone,
        String email,
        String avatar,
        Role role,

        boolean isActive,

        // Données financières du compte
        BigDecimal unpaidRegistrationAmount,  // frais d'inscription impayés
        BigDecimal solidarityAmount,          // cotisation solidarité due
        BigDecimal borrowAmount,              // montant emprunté
        BigDecimal unpaidRenfoulement,        // renflouement impayé
        Long idAccount,
        String pin,


        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}