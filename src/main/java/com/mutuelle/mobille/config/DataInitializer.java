package com.mutuelle.mobille.config;

import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.Admin;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.repository.AdminRepository;
import com.mutuelle.mobille.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthUserRepository authUserRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Très important pour que les deux saves soient dans la même transaction
    public void run(String... args) {
        createSuperAdminIfNotExists();
        createDefaultAdminIfNotExists();
    }

    private void createSuperAdminIfNotExists() {
        String email = "superadmin@mutuelle.com";

        if (authUserRepository.existsByEmail(email)) {
            return;
        }

        // 1. Créer et sauvegarder l'Admin d'abord
        Admin superAdmin = new Admin();
        superAdmin.setFullName("Super Administrateur");
        superAdmin.setIsActive(true);
        superAdmin = adminRepository.saveAndFlush(superAdmin);

        // 2. Créer l'AuthUser avec la référence correcte
        AuthUser authUser = new AuthUser();
        authUser.setEmail(email);
        authUser.setPasswordHash(passwordEncoder.encode("admin123"));
        authUser.setRole(Role.SUPER_ADMIN);
        authUser.setUserRefId(superAdmin.getId());

        authUserRepository.save(authUser);

        System.out.println("Super Admin créé avec succès : " + email + " (mot de passe : admin123)");
    }

    private void createDefaultAdminIfNotExists() {
        String email = "admin@mutuelle.com";

        if (authUserRepository.existsByEmail(email)) {
            return;
        }

        Admin admin = new Admin();
        admin.setFullName("Administrateur");
        admin.setIsActive(true);
        admin = adminRepository.saveAndFlush(admin);

        AuthUser authUser = new AuthUser();
        authUser.setEmail(email);
        authUser.setPasswordHash(passwordEncoder.encode("admin123"));
        authUser.setRole(Role.ADMIN);
        authUser.setUserRefId(admin.getId());

        authUserRepository.save(authUser);

        System.out.println("Admin classique créé avec succès : " + email + " (mot de passe : admin123)");
    }
}