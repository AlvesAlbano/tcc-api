package com.example.demo.Controller;

import com.example.demo.Model.RecommendationDTO;
import com.example.demo.Model.UserPcInfoDTO;
import com.example.demo.Service.RecommendationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable String steamId
    ) throws IOException, InterruptedException {
        return recommendationService.recommendGames(steamId);
    }

    // Modo hardware
    @PostMapping("/{steamId}")
    public List<RecommendationDTO> recommendWithHardware(
            @PathVariable String steamId,
            @RequestBody UserPcInfoDTO userPcInfoDTO
    ) throws IOException, InterruptedException {
        return recommendationService.recommendGames(steamId, userPcInfoDTO);
    }
}
