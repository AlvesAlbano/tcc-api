
package com.example.demo.Controller;

import com.example.demo.Service.GameCatalogService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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
}
