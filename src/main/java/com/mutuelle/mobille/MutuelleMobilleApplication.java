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

		};
	}
}