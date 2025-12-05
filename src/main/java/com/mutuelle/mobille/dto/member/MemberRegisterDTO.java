package com.mutuelle.mobille.dto.member;

import jakarta.validation.constraints.*;

public record MemberRegisterDTO(

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 50)
        String firstname,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 50)
        String lastname,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^(\\+?228)?[ -]?[0-9]{8}$",
                message = "Numéro de téléphone togolais invalide")
        String phone,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
        String password

) {}