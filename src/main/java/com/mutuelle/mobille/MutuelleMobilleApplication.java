package com.mutuelle.mobille;

import com.mutuelle.mobille.service.EmailService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAsync
public class MutuelleMobilleApplication {

	private final EmailService emailService;

	@Autowired
	public MutuelleMobilleApplication(EmailService emailService) {
		this.emailService = emailService;
	}

	public static void main(String[] args) {
		SpringApplication.run(MutuelleMobilleApplication.class, args);
	}

//	/**
//	 * Envoyer un email de test une fois que l'application est complètement démarrée
//	 */
//	@PostConstruct
//	public void sendTestEmailOnStartup() {
//		try {
//			emailService.sendSimpleEmail(
//					"pandoraanimp@gmail.com",
//					"Test démarrage application Mutuelle Mobile",
//					"L'application backend Mutuelle Mobile vient de démarrer avec succès !\n\n" +
//							"Date/Heure : " + java.time.LocalDateTime.now() +
//							"\nEnvironnement : " + System.getenv("SPRING_PROFILES_ACTIVE")
//			);
//			System.out.println("Email de test envoyé avec succès");
//		} catch (Exception e) {
//			System.err.println("Échec envoi email de test : " + e.getMessage());
//			e.printStackTrace();
//		}
//	}

	/**
	 * Ping régulier vers l'API Render pour éviter la mise en veille (cold start)
	 */
	@PostConstruct
	public void startKeepAlivePinger() {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> {
			try {
				URL url = new URL("https://mutuelle-mobile-api-2025.onrender.com/api/config/current");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000); // 5 secondes max
				conn.setReadTimeout(5000);
				conn.connect();

				int code = conn.getResponseCode();
				System.out.println("Ping Render → code: " + code + " | " + java.time.LocalDateTime.now());

				conn.disconnect();
			} catch (Exception e) {
				System.err.println("Échec ping Render : " + e.getMessage());
			}
		}, 0, 12, TimeUnit.MINUTES); // Toutes les 12 minutes
	}
}