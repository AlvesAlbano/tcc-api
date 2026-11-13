package com.example.demo.Service;

import com.example.demo.Entity.Game;
import com.example.demo.Entity.GameRequirement;
import com.example.demo.Entity.RequirementType;
import com.example.demo.Entity.Tag;
import com.example.demo.Model.*;
import com.example.demo.Repository.GameRepository;
import com.example.demo.Model.RecommendationCandidate;
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

    private static final int DEFAULT_RECOMMENDATIONS = 20;
    private static final int MIN_RECOMMENDATIONS = 4;
    private static final int MAX_RECOMMENDATIONS = 50;

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

    // Modo descoberta: somente gosto do usuário (qtd padrão)
    @Transactional(readOnly = true)
    public List<RecommendationDTO> recommendGames(String steamId) throws IOException, InterruptedException {
        return recommendGames(steamId, null, DEFAULT_RECOMMENDATIONS);
    }

    // Modo descoberta: somente gosto do usuário
    @Transactional(readOnly = true)
    public List<RecommendationDTO> recommendGames(String steamId, int quantity) throws IOException, InterruptedException {
        return recommendGames(steamId, null, quantity);
    }

    // Modo hardware: gosto + compatibilidade (qtd padrão)
    @Transactional(readOnly = true)
    public List<RecommendationDTO> recommendGames(
            String steamId,
            UserPcInfoDTO userPcInfoDTO
    ) throws IOException, InterruptedException {
        return recommendGames(steamId, userPcInfoDTO, DEFAULT_RECOMMENDATIONS);
    }

    // Modo hardware: gosto do usuário + compatibilidade
    @Transactional(readOnly = true)
    public List<RecommendationDTO> recommendGames(
            String steamId,
            UserPcInfoDTO userPcInfoDTO,
            int quantity
    ) throws IOException, InterruptedException {

        if (quantity < MIN_RECOMMENDATIONS || quantity > MAX_RECOMMENDATIONS) {
            throw new IllegalArgumentException(
                    "A quantidade deve estar entre 5 e 50."
            );
        }

        UserProfileDTO profile = profileService.buildProfile(steamId);
        List<Game> catalog = gameRepository.findAll();
        List<RecommendationCandidate> candidates = new ArrayList<>();

        for (Game game : catalog) {
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

            candidates.add(
                    new RecommendationCandidate(
                            game,
                            tags,
                            similarityScore
                    )
            );
        }

        candidates.sort(
                Comparator.comparingDouble(RecommendationCandidate::score)
                        .reversed()
        );

        List<RecommendationCandidate> selectedGames = candidates.stream()
                .limit(quantity)
                .toList();

        List<RecommendationDTO> recommendations = new ArrayList<>();

        for (RecommendationCandidate candidate : selectedGames) {
            Game game = candidate.game();
            GameRequirementsDTO requirements = gameRequirementsService.getRequirements(game.getAppId());
            RequirementsResultsDTO compatibility = null;

            if (userPcInfoDTO != null) {
                compatibility = systemRequirementsCheckerService.compareRequirements(requirements, userPcInfoDTO);
            }

            recommendations.add(
                    new RecommendationDTO(
                            game.getAppId(),
                            game.getName(),
                            game.getShortDescription(),
                            game.getImageUrl(),
                            game.getReleaseYear(),
                            candidate.tags(),
                            requirements,
                            compatibility,
                            candidate.score()
                    )
            );
        }

        return recommendations;
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
