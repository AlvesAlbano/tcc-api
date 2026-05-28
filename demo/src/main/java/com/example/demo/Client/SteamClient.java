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

    // private final WebClient webClient = WebClient.create();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${steam.api.key}")
    private String apiKey;

    private final Map<Integer, List<TagDTO>> tagsCache = new HashMap<>();

    public SteamClient() {

    }

    public String getSteamID(String username) throws JsonMappingException, JsonProcessingException {

        final String urlModel = String.format(
                "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=%s&vanityurl=%s", apiKey,
                username);

        String json = restTemplate.getForObject(urlModel, String.class);
        JsonNode root = objectMapper.readTree(json);

        JsonNode responseNode = root.path("response");

        String steamID = responseNode.path("steamid").asText();

        return steamID;
    }

    public JsonNode getGames(String username) throws JsonMappingException, JsonProcessingException {
        String steamId = getSteamID(username);

        final String urlRequest = String.format("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=true",
                apiKey, steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        return root.path("response").path("games");
    }

    public List<GameDTO> getGamesTyped(String username) throws JsonProcessingException {
        JsonNode gamesNode = getGames(username);

        List<GameDTO> games = new ArrayList<>();

        for (JsonNode gameNode : gamesNode) {
            int appId = gameNode.path("appid").asInt();
            String name = gameNode.path("name").asText();
            int playtime = gameNode.path("playtime_forever").asInt();

            games.add(new GameDTO(appId, name, playtime));
        }

        return games;
    }

    public List<TagDTO> getGameTags(int appId) throws JsonProcessingException {
        if (tagsCache.containsKey(appId)) {
            return tagsCache.get(appId);
        }

        final String urlRequest = String.format(
                "https://steamspy.com/api.php?request=appdetails&appid=%d",
                appId
        );

        String json = restTemplate.getForObject(urlRequest, String.class);
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

    public JsonNode getGameInfo(int appId) throws JsonMappingException, JsonProcessingException {
        final String urlRequest = String.format("https://store.steampowered.com/api/appdetails?appids=%d", appId);

        String json = restTemplate.getForObject(urlRequest, String.class);
        JsonNode root = objectMapper.readTree(json);

        JsonNode appNode = root.path(String.valueOf(appId));
        JsonNode dataNode = appNode.path("data");

        return dataNode;
    }

    public JsonNode getFriends(String username) throws JsonMappingException, JsonProcessingException {
        String steamId = getSteamID(username);

        final String urlRequest = String.format("https://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=%s&steamid=%s&relationship=all", apiKey, steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode friendslistNode = root.path("friendslist");
        JsonNode friendsNode = friendslistNode.path("friends");

        return friendsNode;
    }

    public JsonNode getWishlist(String username) throws JsonMappingException, JsonProcessingException {
        String steamId = getSteamID(username);

        final String urlRequest = String.format("https://api.steampowered.com/IWishlistService/GetWishlist/v1/?steamid=%s", steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode response = root.path("response");
        JsonNode wishlist = response.path("items");

        return wishlist;
    }

    public JsonNode getAccountInfo(String username) throws JsonMappingException, JsonProcessingException {
        String steamId = getSteamID(username);

        final String urlRequest = String.format("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=%s&steamids=%s",
                apiKey, steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode accountInfo = root.path("response").path("players");

        return accountInfo;
    }

}
