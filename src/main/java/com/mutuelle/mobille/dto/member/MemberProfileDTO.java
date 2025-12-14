package com.mutuelle.mobille.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String phone;
    private String avatar;
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Les données du compte
    private AccountMemberDTO account;
}