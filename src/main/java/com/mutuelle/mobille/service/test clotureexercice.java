@Test
void testCloturerExercice_DoitTransfererEpargneVersCaisse() {
    // Arrange
    Exercice exercice = creerExerciceAvecTransactions();
    
    // Act
    Exercice exerceCloture = clotureExerciceService.cloturerExercice(
        exercice.getId(), 
        LocalDate.of(2025, 12, 31)
    );
    
    // Assert
    assertThat(exerceCloture.getStatut()).isEqualTo(Exercice.StatutExercice.CLOTURE);
    assertThat(exerceCloture.getEpargneTotale()).isGreaterThan(0);
    assertThat(exerceCloture.getRenflouementDu()).isGreaterThan(0);
    
    // Vérifier la caisse mutuelle
    CaisseMutuelle caisse = caisseMutuelleRepository.findById(1L).orElseThrow();
    assertThat(caisse.getEpargneTotal()).isGreaterThan(0);
    assertThat(caisse.getRenflouementImpaye()).isGreaterThan(0);
}