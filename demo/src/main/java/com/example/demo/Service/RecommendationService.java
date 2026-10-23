package com.example.demo.Service;

import com.example.demo.Entity.Game;
import com.example.demo.Entity.GameRequirement;
import com.example.demo.Entity.RequirementType;
import com.example.demo.Entity.Tag;
import com.example.demo.Model.GameRequirementsDTO;
import com.example.demo.Model.RecommendationDTO;
import com.example.demo.Model.RequirementsSetDTO;
import com.example.demo.Model.UserProfileDTO;
import com.example.demo.Repository.GameRepository;
import com.example.demo.Repository.GameRequirementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final ProfileService profileService;
    private final GameRepository gameRepository;
    private final GameRequirementRepository requirementRepository;
    private final ObjectMapper objectMapper;

    public RecommendationService(
            ProfileService profileService,
            GameRepository gameRepository,
            GameRequirementRepository requirementRepository,
            ObjectMapper objectMapper) {
        this.profileService = profileService;
        this.gameRepository = gameRepository;
        this.requirementRepository = requirementRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
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
                List<String> tags = game.getTags().stream().map(Tag::getName).sorted().toList();
                GameRequirementsDTO requirements = mapRequirements(game.getAppId());
                recommendations.add(
                        new RecommendationDTO(
                                game.getAppId(), game.getName(), game.getShortDescription(), game.getImageUrl(),
                                game.getReleaseYear(), tags, requirements, score
                        )
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

    private GameRequirementsDTO mapRequirements(Integer appId) throws JsonProcessingException {
        RequirementsSetDTO minimum = null;
        RequirementsSetDTO recommended = null;

        for (GameRequirement item : requirementRepository.findAllByGameAppId(appId)) {
            RequirementsSetDTO dto = new RequirementsSetDTO(
                    readList(item.getCpuOptionsJson()), readList(item.getGpuOptionsJson()),
                    item.getRamGb(), item.getStorageGb());

            if (item.getType() == RequirementType.MINIMUM) {
                minimum = dto;
            }
            if (item.getType() == RequirementType.RECOMMENDED) {
                recommended = dto;
            }
        }
        if (minimum == null && recommended == null) return null;
        return new GameRequirementsDTO(minimum, recommended);
    }

    private List<String> readList(String json) throws JsonProcessingException {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        return objectMapper.readValue(json, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, String.class));
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
