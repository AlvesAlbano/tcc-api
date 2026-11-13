package com.example.demo.Controller;

import com.example.demo.Service.GameCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.Model.GameRequirementsDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/admin/catalog")
public class CatalogImportController {

    private final GameCatalogService gameCatalogService;

    public CatalogImportController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importGames(
            @RequestParam String tag,
            @RequestParam(defaultValue = "20") int limit
    ) throws Exception {

        if (tag.isBlank() || limit < 1 || limit > 100) {
            return ResponseEntity.badRequest()
                    .body("Informe uma tag válida e um limite entre 1 e 100.");
        }

        gameCatalogService.importGamesByTag(tag, limit);

        return ResponseEntity.ok(
                "Importação da tag '" + tag + "' finalizada."
        );
    }

    @GetMapping("/{appId}/requirements/preview")
    public ResponseEntity<GameRequirementsDTO> previewRequirements(
            @PathVariable int appId
    ) throws Exception {

        GameRequirementsDTO requirements =
                gameCatalogService.previewRequirements(appId);

        return ResponseEntity.ok(requirements);
    }

    @PostMapping("/{appId}/requirements/refresh")
    public ResponseEntity<String> refreshRequirements(
            @PathVariable int appId
    ) throws Exception {

        boolean updated = gameCatalogService.refreshRequirementsForGame(appId);

        if (!updated) {
            return ResponseEntity.unprocessableEntity()
                    .body("Não foi possível atualizar os requisitos desse jogo.");
        }

        return ResponseEntity.ok(
                "Requisitos do jogo " + appId + " atualizados com sucesso."
        );
    }

    @PostMapping("/requirements/refresh")
    public ResponseEntity<String> refreshRequirementsBatch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {

        if (page < 0 || size < 1 || size > 50) {
            return ResponseEntity.badRequest()
                    .body("Página deve ser >= 0 e tamanho deve estar entre 1 e 50.");
        }

        String result = gameCatalogService.refreshRequirementsBatch(page, size);

        return ResponseEntity.ok(result);
    }
}
