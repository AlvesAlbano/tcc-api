package com.example.demo.Service;

import com.example.demo.Client.SteamClient;
import com.example.demo.Client.SteamSpyClient;
import com.example.demo.Entity.Game;
import com.example.demo.Entity.Tag;
import com.example.demo.Repository.GameRepository;
import com.example.demo.Repository.TagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class GameCatalogService {

    private final GameRepository gameRepository;
    private final TagRepository tagRepository;
    private final SteamClient steamClient;
    private final SteamSpyClient steamSpyClient;

    public GameCatalogService(
            GameRepository gameRepository,
            TagRepository tagRepository,
            SteamClient steamClient,
            SteamSpyClient steamSpyClient
    ) {
        this.gameRepository = gameRepository;
        this.tagRepository = tagRepository;
        this.steamClient = steamClient;
        this.steamSpyClient = steamSpyClient;
    }

    public void importGamesByTag(String tagName, int limit) throws Exception {
        JsonNode gamesNode = steamSpyClient.getGamesByTag(tagName);

        Iterator<String> gameIds = gamesNode.fieldNames();

        int count = 0;
        while (gameIds.hasNext() && count < limit) {

            String appIdString = gameIds.next();

            int appId = Integer.parseInt(appIdString);

            if (gameRepository.existsById(appId)) {
                continue;
            }

            try {
                JsonNode gameInfo = steamClient.getGameInfo(appId);

                String gameName = gameInfo.path("name").asText();

                if (gameName == null || gameName.isBlank()) {
                    continue;
                }

                Game game = new Game();

                game.setAppId(appId);
                game.setName(gameName);

                game.setImageUrl(
                        gameInfo.path("header_image").asText()
                );

                game.setShortDescription(
                        gameInfo.path("short_description").asText()
                );

                JsonNode spyDetails = steamSpyClient.getGameDetails(appId);

                JsonNode tagsNode = spyDetails.path("tags");
                Set<Tag> gameTags = new HashSet<>();

                Iterator<String> tagNames = tagsNode.fieldNames();

                while (tagNames.hasNext()) {
                    String tagNameFromApi = tagNames.next();

                    Tag tag = tagRepository
                            .findByName(tagNameFromApi)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();

                                newTag.setName(tagNameFromApi);

                                return tagRepository.save(newTag);
                            });

                    gameTags.add(tag);
                }

                game.setTags(gameTags);
                gameRepository.save(game);

                count++;
                System.out.println("Importado: " + game.getName());

            } catch (Exception e) {
                System.out.println("Erro ao importar jogo: " + appId);
            }
        }

        System.out.println("Importação finalizada!");
    }
}