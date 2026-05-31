package com.example.demo.Client;

import com.example.demo.Model.GameDTO;
import com.example.demo.Model.TagDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Component
public class SteamClient {

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${steam.api.key}")
    private String apiKey;

    private final Map<Integer, List<TagDTO>> tagsCache = new HashMap<>();

    private final Map<String, String> steamIdCache = new HashMap<>();

    public SteamClient() {

    }

    public String resolveSteamId(String steamIdentifier) throws JsonProcessingException {
        // já é steamID64
        if (steamIdentifier.matches("\\d{17}")) {
            return steamIdentifier;
        }

        if (steamIdCache.containsKey(steamIdentifier)) {
            return steamIdCache.get(steamIdentifier);
        }

        final String url = String.format(
                "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=%s&vanityurl=%s",
                apiKey,
                steamIdentifier
        );

        String json = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(json);

        String steamId = root
                .path("response")
                .path("steamid")
                .asText();

        steamIdCache.put(steamIdentifier, steamId);

        return steamId;
    }

    public JsonNode getAccountInfo(String steamId) throws JsonProcessingException {
        final String url = String.format(
                "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=%s&steamids=%s",
                apiKey,
                steamId
        );

        String json = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(json);

        return root.path("response").path("players");
    }

    public JsonNode getGames(String steamId) throws JsonProcessingException {
        final String url = String.format(
                "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=true",
                apiKey,
                steamId
        );

        String json = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(json);

        return root.path("response").path("games");
    }

    public List<GameDTO> getGamesTyped(String steamId) throws JsonProcessingException {
        JsonNode gamesNode = getGames(steamId);

        List<GameDTO> games = new ArrayList<>();

        for (JsonNode gameNode : gamesNode) {
            int appId = gameNode.path("appid").asInt();
            String name = gameNode.path("name").asText();
            int playtime = gameNode.path("playtime_forever").asInt();

            games.add(new GameDTO(appId, name, playtime));
        }

        return games;
    }

    public JsonNode getFriends(String steamId) throws JsonMappingException, JsonProcessingException {
        final String url = String.format(
                "https://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=%s&steamid=%s&relationship=all",
                apiKey,
                steamId
        );

        String json = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(json);

        return root.path("friendslist").path("friends");
    }

    public JsonNode getWishlist(String steamId) throws JsonMappingException, JsonProcessingException {
        final String url = String.format(
                "https://api.steampowered.com/IWishlistService/GetWishlist/v1/?steamid=%s",
                steamId
        );

        String json = restTemplate.getForObject(url, String.class);

        JsonNode root = objectMapper.readTree(json);

        return root.path("response").path("items");
    }

    public JsonNode getGameInfo(int appId) throws JsonMappingException, JsonProcessingException {
        final String url = String.format(
                "https://store.steampowered.com/api/appdetails?appids=%d",
                appId
        );

        String json = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(json);

        return root
                .path(String.valueOf(appId))
                .path("data");
    }

    public List<TagDTO> getGameTags(int appId) throws JsonProcessingException {
        if (tagsCache.containsKey(appId)) {
            return tagsCache.get(appId);
        }

        final String url = String.format(
                "https://steamspy.com/api.php?request=appdetails&appid=%d",
                appId
        );

        String json = restTemplate.getForObject(url, String.class);
        if (json == null || !json.trim().startsWith("{")) {
            return Collections.emptyList();
        }

        JsonNode root = objectMapper.readTree(json);

        JsonNode tagsNode = root.path("tags");
        List<TagDTO> tags = new ArrayList<>();

        if (tagsNode.isObject()) {
            Iterator<String> fieldNames = tagsNode.fieldNames();

            while (fieldNames.hasNext()) {
                String tagName = fieldNames.next();

                if (!tagName.isBlank()) {
                    tags.add(new TagDTO(tagName));
                }
            }
        }

        tagsCache.put(appId, tags);

        return tags;
    }

}
