package com.example.demo.Service;

import com.example.demo.Client.SteamClient;
import com.example.demo.Model.GameDTO;
import com.example.demo.Model.TagDTO;
import com.example.demo.Model.UserProfileDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    // ignorar jogos com menos de 2h
    private static final int MIN_PLAYTIME = 120;
    // limita jogos analisados (consideramos depois remover ou não)
    private static final int MAX_GAMES = 100;

    private final SteamClient steamClient;

    public ProfileService(SteamClient steamClient) {
        this.steamClient = steamClient;
    }

    public UserProfileDTO buildProfile(String steamId) throws JsonProcessingException {
        List<GameDTO> games = steamClient.getGamesTyped(steamId);

        Set<Integer> ownedGames = games.stream()
                .map(GameDTO::appId)
                .collect(Collectors.toSet());

        List<GameDTO> relevantGames = games.stream()
                .filter(game -> game.playtime() >= MIN_PLAYTIME)
                .sorted(Comparator.comparingInt(GameDTO::playtime).reversed())
                .limit(MAX_GAMES)
                .toList();

        Map<String, Double> tagWeights = new HashMap<>();

        for (GameDTO game : relevantGames) {
            List<TagDTO> tags = steamClient.getGameTags(game.appId());

            if (tags == null || tags.isEmpty()) {
                continue;
            }

            double gameWeight = calculateGameWeight(game.playtime());

            for (TagDTO tag : tags) {
                String tagName = normalizeTag(tag.name());

                if (shouldIgnoreTag(tagName)) {
                    continue;
                }

                tagWeights.merge(tagName, gameWeight, Double::sum);
            }
        }

        normalizeWeights(tagWeights);
        return new UserProfileDTO(ownedGames, tagWeights);
    }

    private double calculateGameWeight(int playtime) {
        // peso = log(playtime + 1)
        double hours = playtime / 60.0;
        return Math.log(hours + 1);
    }

    private String normalizeTag(String tag) {
        return tag.trim().toLowerCase();
    }

    private boolean shouldIgnoreTag(String tag) {
        Set<String> ignoredTags = Set.of(
                "indie",
                "casual",
                "singleplayer",
                "multiplayer",
                "action",
                "adventure"
        );

        return ignoredTags.contains(tag);
    }

    private void normalizeWeights(Map<String, Double> weights) {
        double max = weights.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            weights.put(
                    entry.getKey(),
                    entry.getValue() / max
            );
        }
    }

}
