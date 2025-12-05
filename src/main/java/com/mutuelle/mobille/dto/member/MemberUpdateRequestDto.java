package com.mutuelle.mobille.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequestDto(

        @Size(min = 2, max = 50)
        String firstname,

        @Size(min = 2, max = 50)
        String lastname,

        @Pattern(regexp = "^(\\+?228)?[ -]?[0-9]{8}$")
        String phone,

        @Email
        String email,

        String avatar

) {}