package com.mutuelle.mobille.dto.notifications;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushTestDto {

    @NotBlank(message = "Le titre est requis")
    private String title;

    @NotBlank(message = "Le message est requis")
    private String message;

    @NotBlank(message = "L'id user est requis")
    private Long userId;
}