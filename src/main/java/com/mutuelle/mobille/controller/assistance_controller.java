package mutuelle.back.assistance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assistances")
public class AssistanceController {
    
    @Autowired
    private AssistanceService assistanceService;
    
    @Autowired
    private TypeAssistanceService typeAssistanceService;
    
    // 1. Créer une assistance (nouvelle ou réutilisée)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssistanceDTO> creerAssistance(
            @Valid @RequestBody CreerAssistanceRequest request) {
        Assistance assistance = assistanceService.creerAssistance(request);
        return ResponseEntity.ok(AssistanceMapper.toDTO(assistance));
    }
    
    // 2. Lister les assistances par exercice
    @GetMapping("/exercice/{exerciceId}")
    public ResponseEntity<List<AssistanceDTO>> listerParExercice(@PathVariable Long exerciceId) {
        List<Assistance> assistances = assistanceRepository.findByExerciceId(exerciceId);
        return ResponseEntity.ok(AssistanceMapper.toDTOList(assistances));
    }
    
    // 3. Réutiliser une assistance spécifique
    @PostMapping("/{id}/reutiliser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssistanceDTO> reutiliserAssistance(
            @PathVariable Long id,
            @RequestBody ReutiliserAssistanceRequest request) {
        // Créer une copie modifiée
        AssistanceRequest req = new AssistanceRequest();
        req.setAssistanceSourceId(id);
        req.setMontant(request.getMontant());
        req.setMembreId(request.getMembreId());
        
        Assistance assistance = assistanceService.creerAssistance(req);
        return ResponseEntity.ok(AssistanceMapper.toDTO(assistance));
    }
    
    // 4. Gestion des types d'assistance
    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypeAssistanceDTO> creerTypeAssistance(
            @Valid @RequestBody CreerTypeAssistanceRequest request) {
        TypeAssistance type = typeAssistanceService.creerType(request);
        return ResponseEntity.ok(TypeAssistanceMapper.toDTO(type));
    }
}