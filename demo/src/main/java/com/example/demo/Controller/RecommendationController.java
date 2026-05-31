package com.example.demo.Controller;

import com.example.demo.Model.RecommendationDTO;
import com.example.demo.Service.RecommendationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@Tag(name="Recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{steamId}")
    public List<RecommendationDTO> recommend(@PathVariable String steamId) throws JsonProcessingException {
        return recommendationService.recommendGames(steamId);
    }
}
