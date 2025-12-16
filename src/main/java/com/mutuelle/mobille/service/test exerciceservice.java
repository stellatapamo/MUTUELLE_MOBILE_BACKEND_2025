package mutuelle.back.exercise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ExerciceServiceTest {
    
    @Autowired
    private ExerciceService exerciceService;
    
    @Autowired
    private ExerciceRepository exerciceRepository;
    
    @Test
    void testCreerExercice_Success() {
        // Act
        Exercice exercice = exerciceService.creerExercice(
            "Exercice 2025", 
            LocalDate.of(2025, 1, 1)
        );
        
        // Assert
        assertThat(exercice).isNotNull();
        assertThat(exercice.getNom()).isEqualTo("Exercice 2025");
        assertThat(exercice.getStatut()).isEqualTo(Exercice.StatutExercice.ACTIF);
        assertThat(exercice.getSessions()).hasSize(1);
    }
    
    @Test
    void testCreerExercice_QuandExerciceActifExiste_DoitEchouer() {
        // Arrange
        exerciceService.creerExercice("Exercice 2024", LocalDate.of(2024, 1, 1));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            exerciceService.creerExercice("Exercice 2025", LocalDate.of(2025, 1, 1));
        });
    }
}