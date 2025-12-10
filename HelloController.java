package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController indique à Spring que cette classe est un composant de l'API REST
@RestController
public class HelloController {

    // @GetMapping("/hello") associe cette méthode à la requête HTTP GET sur l'URL /hello
    @GetMapping("/hello")
    public String direBonjour() {
        return "Bonjour ! Votre backend Spring Boot 'demo' fonctionne correctement !";
    }

}