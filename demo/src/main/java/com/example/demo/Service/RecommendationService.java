package com.example.demo.Service;

import com.example.demo.Entity.Game;
import com.example.demo.Entity.Tag;
import com.example.demo.Model.RecommendationDTO;
import com.example.demo.Model.UserProfileDTO;
import com.example.demo.Repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    private final ProfileService profileService;
    private final GameRepository gameRepository;

    public RecommendationService(ProfileService profileService, GameRepository gameRepository) {
        this.profileService = profileService;
        this.gameRepository = gameRepository;
    }

    public List<RecommendationDTO> recommendGames(String steamId) throws JsonProcessingException {
        UserProfileDTO profile = profileService.buildProfile(steamId);

        List<Game> catalog = gameRepository.findAll();

        List<RecommendationDTO> recommendations = new ArrayList<>();

        for (Game game : catalog) {
            // não recomenda jogo já possuído
            if (profile.ownedGames().contains(game.getAppId())) {
                continue;
            }

            double score = calculateCossineSimilarity(profile, game);

            if (score > 0) {
                recommendations.add(
                        new RecommendationDTO(game.getAppId(), game.getName(), game.getShortDescription(), game.getImageUrl(), score)
                );
            }
        }

        recommendations.sort(
                Comparator.comparingDouble(RecommendationDTO::score).reversed()
        );

        return recommendations.stream()
                .limit(20)
                .toList();
    }

    private double calculateCossineSimilarity(UserProfileDTO profile, Game game) {
        Map<String, Double> userVector = profile.tagWeights();

        double dotProduct = 0.0;
        double userNorm = 0.0;
        double gameNorm = 0.0;

        // norma usuário
        for (double weight : userVector.values()) {
            userNorm += weight * weight;
        }

        // vetor jogos
        for (Tag tag : game.getTags()) {
            String tagName = tag.getName().trim().toLowerCase();

            double gameWeight = 1.0;

            gameNorm += gameWeight * gameWeight;

            Double userWeight = userVector.get(tagName);

            if (userWeight != null) {
                dotProduct += userWeight * gameWeight;
            }
        }

        userNorm = Math.sqrt(userNorm);
        gameNorm = Math.sqrt(gameNorm);

        if (userNorm == 0 || gameNorm == 0) {
            return 0.0;
        }

        return dotProduct / (userNorm * gameNorm);
    }

}
