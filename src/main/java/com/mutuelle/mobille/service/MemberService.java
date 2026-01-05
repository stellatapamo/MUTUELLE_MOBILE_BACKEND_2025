package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.member.MemberRegisterDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.repository.AuthUserRepository;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MutuelleConfigService mutuelleConfigService;
    private final MemberRepository memberRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    // ===========================================================================
    // INSCRIPTION MEMBRE → Crée Member + Account + AuthUser (comme le superadmin)
    // ===========================================================================
    @Transactional
    public MemberResponseDTO registerMember(MemberRegisterDTO dto) {
        MutuelleConfig config = mutuelleConfigService.getCurrentConfig();
        if (memberRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé");
        }
        if (authUserRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // 1. Création du compte financier
        AccountMember accountMember = AccountMember.builder()
                .unpaidRegistrationAmount(config.getRegistrationFeeAmount())
                .savingAmount(BigDecimal.ZERO)
                .solidarityAmount(BigDecimal.ZERO)
                .borrowAmount(BigDecimal.ZERO)
                .unpaidRenfoulement(BigDecimal.ZERO)
                .unpaidSolidarityAmount(BigDecimal.ZERO)
                .isActive(true)
                .build();

        // 2. Création du membre
        Member member = Member.builder()
                .firstname(dto.getFirstname().trim())
                .lastname(dto.getLastname().trim())
                .phone(dto.getPhone().trim())
                .isActive(true)
                .accountMember(accountMember)
                .build();

        accountMember.setMember(member); // bidirectionnel
        member = memberRepository.save(member); // cascade → compte sauvé aussi

        // 3. Création de l'AuthUser (EXACTEMENT comme ton superadmin)
        AuthUser authUser = new AuthUser();
        authUser.setEmail(dto.getEmail().toLowerCase().trim());
        authUser.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        authUser.setRole(Role.MEMBER);
        authUser.setUserRefId(member.getId());
        authUser.setPin(dto.getPin());

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
        AccountMember accountMember = member.getAccountMember();
        AuthUser authUser = authUserRepository.findByUserRefId(member.getId())
                .orElse(null);

        String email = authUser != null ? authUser.getEmail() : null;
        Role role = authUser != null ? authUser.getRole() : null;

        return new MemberResponseDTO(
                member.getId(),
                member.getFirstname(),
                member.getLastname(),
                member.getPhone(),
                email,
                member.getAvatar(),
                role,
                member.isActive(),
                accountMember.getUnpaidRegistrationAmount(),
                accountMember.getSolidarityAmount(),
                accountMember.getBorrowAmount(),
                accountMember.getUnpaidRenfoulement(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDTO> getAllActiveMembers() {
        return memberRepository.findAllActiveWithAccount()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MemberResponseDTO> getMembersFiltered(
            String search,
            Boolean active,
            LocalDate createdAfter,
            LocalDate createdBefore,
            Pageable pageable) {

        // Construction des prédicats dynamiques
        Specification<Member> spec = Specification.where(null);

        // Filtre recherche (prénom, nom, téléphone)
        if (search != null && !search.trim().isEmpty()) {
            String term = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstname")), term),
                            cb.like(cb.lower(root.get("lastname")), term),
                            cb.like(root.get("phone"), term)
                    ));
        }

        // Filtre actif/inactif
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), active));
        } else {
            // Par défaut : seulement les actifs (comme ton findAllActiveWithAccount)
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isActive")));
        }

        // Filtre date création après
        if (createdAfter != null) {
            LocalDateTime start = createdAfter.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start));
        }

        // Filtre date création avant
        if (createdBefore != null) {
            LocalDateTime end = createdBefore.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), end));
        }

        // Requête avec fetch join + pagination
        Page<Member> page = memberRepository.findAll(spec, pageable);

        return page.map(this::toResponseDTO);
    }
}
