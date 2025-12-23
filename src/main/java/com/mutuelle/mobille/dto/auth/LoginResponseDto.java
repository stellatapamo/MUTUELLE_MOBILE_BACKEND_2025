package com.mutuelle.mobille.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mutuelle.mobille.dto.admin.AdminProfileDTO;
import com.mutuelle.mobille.dto.member.MemberProfileDTO;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Session;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // N'inclut que les champs non-null
public class LoginResponseDto {
    private Role role;
    private Long userRefId;
    private String ping;
    private String accessToken;
    private String refreshToken;

    // Profils typés (un seul sera non-null)
    private Object profile;
}