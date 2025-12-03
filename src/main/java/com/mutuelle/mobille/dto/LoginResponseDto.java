package com.mutuelle.mobille.dto;

import com.mutuelle.mobille.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private Role userType;
    private Long userRefId;
    private String accessToken;
    private String refreshToken;
    private Object profile; // Tu peux typer plus précisément plus tard (MemberDTO / AdminDTO)
}