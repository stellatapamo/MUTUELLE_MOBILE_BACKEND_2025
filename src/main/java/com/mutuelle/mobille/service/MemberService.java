package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.member.MemberRegisterDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.Account;
import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.repository.AuthUserRepository;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthUserRepository authUserRepository;  // On injecte directement le repo
    private final PasswordEncoder passwordEncoder;

    // ===========================================================================
    // INSCRIPTION MEMBRE → Crée Member + Account + AuthUser (comme le superadmin)
    // ===========================================================================
    @Transactional
    public MemberResponseDTO registerMember(MemberRegisterDTO dto) {

        if (memberRepository.existsByPhone(dto.phone())) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé");
        }
        if (authUserRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // 1. Création du compte financier
        Account account = Account.builder()
                .unpaidRegistrationAmount(new BigDecimal("5000.00"))
                .solidarityAmount(BigDecimal.ZERO)
                .borrowAmount(BigDecimal.ZERO)
                .unpaidRenfoulement(BigDecimal.ZERO)
                .globalAccount(false)
                .isActive(true)
                .build();

        // 2. Création du membre
        Member member = Member.builder()
                .firstname(dto.firstname().trim())
                .lastname(dto.lastname().trim())
                .phone(dto.phone().trim())
                .isActive(true)
                .account(account)
                .build();

        account.setMember(member); // bidirectionnel
        member = memberRepository.save(member); // cascade → compte sauvé aussi

        // 3. Création de l'AuthUser (EXACTEMENT comme ton superadmin)
        AuthUser authUser = new AuthUser();
        authUser.setEmail(dto.email().toLowerCase().trim());
        authUser.setPasswordHash(passwordEncoder.encode(dto.password()));
        authUser.setRole(Role.MEMBER);           // ou userType selon ton enum
        authUser.setUserRefId(member.getId());

        authUserRepository.save(authUser);

        return toResponseDTO(member);
    }

    // ===========================================================================
    // PROFIL DU MEMBRE CONNECTÉ
    // ===========================================================================
    @Transactional(readOnly = true)
    public MemberResponseDTO getCurrentMemberProfile() {
        Long currentMemberId = SecurityUtil.getCurrentUserRefId();
        return getMemberById(currentMemberId);
    }

    // ===========================================================================
    // RÉCUPÉRER UN MEMBRE PAR ID
    // ===========================================================================
    @Transactional(readOnly = true)
    public MemberResponseDTO getMemberById(Long id) {
        Member member = memberRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new IllegalArgumentException("Membre non trouvé"));

        return toResponseDTO(member);
    }

    // ===========================================================================
    // MISE À JOUR AVATAR
    // ===========================================================================
    @Transactional
    public void updateAvatar(String avatarUrl) {
        Long memberId = SecurityUtil.getCurrentUserRefId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Membre introuvable"));

        member.setAvatar(avatarUrl);
        memberRepository.save(member);
    }

    // ===========================================================================
    // MAPPER → DTO
    // ===========================================================================
    private MemberResponseDTO toResponseDTO(Member member) {
        Account account = member.getAccount();

        return new MemberResponseDTO(
                member.getId(),
                member.getFirstname(),
                member.getLastname(),
                member.getPhone(),
                null, // ou tu peux faire une jointure pour récupérer l'email si tu veux
                member.getAvatar(),
                member.isActive(),
                account.getUnpaidRegistrationAmount(),
                account.getSolidarityAmount(),
                account.getBorrowAmount(),
                account.getUnpaidRenfoulement(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}