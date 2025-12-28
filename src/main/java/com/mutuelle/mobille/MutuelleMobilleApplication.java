package com.mutuelle.mobille;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class MutuelleMobilleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MutuelleMobilleApplication.class, args);
	}

	@PostConstruct
	public void startPinger() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(() -> {
			try {
				URL url = new URL("https://mutuelle-mobile-api-2025.onrender.com/api/config/current"); // ou /actuator si vous avez Spring Boot Actuator
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.connect();
				int code = conn.getResponseCode();
				System.out.println("Ping response: " + code);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 0, 12, TimeUnit.MINUTES); // Toutes les 12 minutes
	}
}