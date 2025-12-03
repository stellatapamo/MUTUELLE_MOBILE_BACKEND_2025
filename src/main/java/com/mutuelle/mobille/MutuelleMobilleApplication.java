package com.mutuelle.mobille;

import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.Admin;
import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.repository.AdminRepository;
import com.mutuelle.mobille.repository.AuthUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MutuelleMobilleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MutuelleMobilleApplication.class, args);
	}

	// ADMIN PAR DÉFAUT AUTOMATIQUE
	@Bean
	CommandLineRunner createDefaultAdmin(
			AuthUserRepository authUserRepository,
			AdminRepository adminRepository,
			PasswordEncoder passwordEncoder) {

		return args -> {
			if (authUserRepository.count() == 0) {

				// 1. Création de l'admin dans la table "admins"
				Admin admin = new Admin();
				admin.setFullName("Administrateur");
				admin.setIsActive(true);
				adminRepository.save(admin);

				// 2. Création du compte de connexion
				AuthUser authUser = new AuthUser();
				authUser.setEmail("admin@mutuelle.com");
				authUser.setPasswordHash(passwordEncoder.encode("admin123"));
				authUser.setUserType(Role.ADMIN);
				authUser.setUserRefId(admin.getId());
				authUserRepository.save(authUser);

				System.out.println("========================================");
				System.out.println("ADMINISTRATEUR CRÉÉ AUTOMATIQUEMENT !");
				System.out.println("Email        → admin@mutuelle.com");
				System.out.println("Mot de passe → admin123");
				System.out.println("Tu peux te connecter immédiatement !");
				System.out.println("========================================");
			} else {
				System.out.println("Des utilisateurs existent déjà → aucun admin créé.");
			}
		};
	}
}