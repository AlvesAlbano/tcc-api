package com.example.demo.Service;

import com.example.demo.Entity.Game;
import com.example.demo.Entity.GameRequirement;
import com.example.demo.Entity.RequirementType;
import com.example.demo.Entity.Tag;
import com.example.demo.Model.*;
import com.example.demo.Repository.GameRepository;
import com.example.demo.Repository.GameRequirementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private static final double SIMILARITY_WEIGHT = 0.70;
    private static final double HARDWARE_WEIGHT = 0.30;
    private static final int MAX_RECOMMENDATIONS = 20;

    private final ProfileService profileService;
    private final GameRepository gameRepository;
    private final GameRequirementsService gameRequirementsService;
    private final SystemRequirementsCheckerService systemRequirementsCheckerService;

    public RecommendationService(
            ProfileService profileService,
            GameRepository gameRepository,
            GameRequirementsService gameRequirementsService,
            SystemRequirementsCheckerService systemRequirementsCheckerService
    ) {
        this.profileService = profileService;
        this.gameRepository = gameRepository;
        this.gameRequirementsService = gameRequirementsService;
        this.systemRequirementsCheckerService = systemRequirementsCheckerService;
    }

    // Modo descoberta: somente gosto do usuário.
    @Transactional(readOnly = true)
    public List<RecommendationDTO> recommendGames(String steamId) throws IOException, InterruptedException {
        return recommendGames(steamId, null);
    }

    // Modo hardware: gosto do usuário + compatibilidade.
    @Transactional(readOnly = true)
    public List<RecommendationDTO> recommendGames(
            String steamId,
            UserPcInfoDTO userPcInfoDTO
    ) throws IOException, InterruptedException {

        UserProfileDTO profile = profileService.buildProfile(steamId);
        List<Game> catalog = gameRepository.findAll();
        List<RecommendationDTO> recommendations = new ArrayList<>();

        for (Game game : catalog) {
            // Não recomenda jogos que o usuário já possui.
            if (profile.ownedGames().contains(game.getAppId())) {
                continue;
            }

            double similarityScore = calculateCossineSimilarity(profile, game);

            if (similarityScore <= 0) {
                continue;
            }

            List<String> tags = game.getTags()
                    .stream()
                    .map(Tag::getName)
                    .sorted()
                    .toList();

            GameRequirementsDTO requirements = gameRequirementsService.getRequirements(game.getAppId());

            // MODO DESCOBERTA
            if (userPcInfoDTO == null) {
                recommendations.add(
                        new RecommendationDTO(
                                game.getAppId(),
                                game.getName(),
                                game.getShortDescription(),
                                game.getImageUrl(),
                                game.getReleaseYear(),
                                tags,
                                requirements,
                                null,
                                similarityScore
                        )
                );

                continue;
            }

            // MODO HARDWARE
            RequirementsResultsDTO compatibility =
                    systemRequirementsCheckerService.compareRequirements(
                            requirements,
                            userPcInfoDTO
                    );

            // Descarta jogos comprovadamente incompatíveis
            // com algum requisito mínimo.
            if (!systemRequirementsCheckerService.meetsMinimumRequirements(compatibility)) {
                continue;
            }

            double hardwareScore = systemRequirementsCheckerService.calculateHardwareScore(compatibility);

            double finalScore = (similarityScore * SIMILARITY_WEIGHT) + (hardwareScore * HARDWARE_WEIGHT);

            recommendations.add(
                    new RecommendationDTO(
                            game.getAppId(),
                            game.getName(),
                            game.getShortDescription(),
                            game.getImageUrl(),
                            game.getReleaseYear(),
                            tags,
                            requirements,
                            compatibility,
                            finalScore
                    )
            );
        }

        // Ordena pela pontuação final.
        recommendations.sort(
                Comparator.comparingDouble(RecommendationDTO::score)
                        .reversed()
        );

        return recommendations.stream()
                .limit(MAX_RECOMMENDATIONS)
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
