package com.mutuelle.mobille.dto.member;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegisterDTO {

        @NotNull(message = "Le prénom est obligatoire")      // bloque explicitement null
        @NotBlank(message = "Le prénom ne peut pas être vide ou composé uniquement d'espaces")
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        private String firstname;

        @NotNull(message = "Le nom est obligatoire")
        @NotBlank(message = "Le nom ne peut pas être vide ou composé uniquement d'espaces")
        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        private String lastname;

        @NotNull(message = "Le téléphone est obligatoire")
        @NotBlank(message = "Le téléphone ne peut pas être vide")
        @Pattern(regexp = "^(\\+?228)?[ -]?[0-9]{8}$",
                message = "Numéro de téléphone togolais invalide (ex: 90123456 ou +22890123456)")
        private String phone;

        @NotNull(message = "L'email est obligatoire")
        @NotBlank(message = "L'email ne peut pas être vide")
        @Email(message = "Format d'email invalide")
        private String email;

        @NotNull(message = "Le mot de passe est obligatoire")
        @NotBlank(message = "Le mot de passe ne peut pas être vide")
        @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
        private String password;

}