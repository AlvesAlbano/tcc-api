package com.example.demo.Controller;

import com.example.demo.Model.RecommendationDTO;
import com.example.demo.Model.UserPcInfoDTO;
import com.example.demo.Service.RecommendationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/recommendations")
@Tag(name = "Recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // Modo descoberta
    @GetMapping("/{steamId}")
    public List<RecommendationDTO> recommend(
            @PathVariable String steamId,
            @RequestParam(defaultValue = "20") int quantity
    ) throws IOException, InterruptedException {
        validateQuantity(quantity);
        return recommendationService.recommendGames(steamId, quantity);
    }

    // Modo hardware
    @PostMapping("/{steamId}")
    public List<RecommendationDTO> recommendWithHardware(
            @PathVariable String steamId,
            @RequestBody UserPcInfoDTO userPcInfoDTO,
            @RequestParam(defaultValue = "20") int quantity
    ) throws IOException, InterruptedException {
        validateQuantity(quantity);
        return recommendationService.recommendGames(steamId, userPcInfoDTO, quantity);
    }

    private void validateQuantity(int quantity) {
        if (quantity < 4 || quantity > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade de recomendações deve estar entre 4 e 50."
            );
        }
    }
}
