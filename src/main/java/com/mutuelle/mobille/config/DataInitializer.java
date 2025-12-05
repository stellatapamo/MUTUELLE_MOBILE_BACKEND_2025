package com.mutuelle.mobille.config;

import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.Admin;
import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.repository.AdminRepository;
import com.mutuelle.mobille.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DataInitializer implements CommandLineRunner {

    private final AuthUserRepository authUserRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!authUserRepository.existsByEmail("superadmin@mutuelle.com")) {
            Admin superAdmin = new Admin();
            superAdmin.setFullName("Super Administrateur");
            superAdmin.setIsActive(true);
            adminRepository.save(superAdmin);

            AuthUser auth = new AuthUser();
            auth.setEmail("superadmin@mutuelle.com");
            auth.setPasswordHash(passwordEncoder.encode("admin123"));
            auth.setRole(Role.SUPER_ADMIN);
            auth.setUserRefId(superAdmin.getId());
            authUserRepository.save(auth);

            System.out.println("Super Admin créé : superadmin@mutuelle.com");
        }
    }
}