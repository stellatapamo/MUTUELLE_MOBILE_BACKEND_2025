package com.mutuelle.mobille.mapper;

import com.mutuelle.mobille.dto.admin.AdminProfileDTO;
import com.mutuelle.mobille.models.Admin;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public AdminProfileDTO toProfileDTO(Admin admin) {
        if (admin == null) return null;

        return AdminProfileDTO.builder()
                .id(admin.getId())
                .avatar(admin.getFullName())
                .isActive(admin.getIsActive())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();
    }
}