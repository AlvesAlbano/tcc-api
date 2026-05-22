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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public String getSteamID(String nomeUsuario) throws JsonMappingException, JsonProcessingException {

        final String urlModel = String.format(
                "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=%s&vanityurl=%s", apiKey,
                nomeUsuario);

        String json = restTemplate.getForObject(urlModel, String.class);
        JsonNode root = objectMapper.readTree(json);

        JsonNode responseNode = root.path("response");

        String steamID = responseNode.path("steamid").asText();

        return steamID;
    }

    public JsonNode getGames(String steamId) throws JsonMappingException, JsonProcessingException {

        // final String urlRequest = String.format(
        //         "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=%s&steamid=%s&format=json",
        //         chaveApi, steamId);
        final String urlRequest = String.format("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=true",
                apiKey, steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);
        JsonNode root = objectMapper.readTree(json);

        JsonNode responseNode = root.path("response");
        JsonNode gamesList = responseNode.path("games");

        return gamesList;
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

    public List<TagDTO> getGameTags(int appId) throws JsonProcessingException {
        if (tagsCache.containsKey(appId)) {
            return tagsCache.get(appId);
        }

        JsonNode gameInfo = getGameInfo(appId);
        JsonNode categoriesNode = gameInfo.path("categories");

        List<TagDTO> tags = new ArrayList<>();

        if (categoriesNode.isArray()) {
            for (JsonNode category : categoriesNode) {
                String description = category.path("description").asText();

                if (!description.isBlank()) {
                    tags.add(new TagDTO(description));
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

    public JsonNode getFriends(String steamId) throws JsonMappingException, JsonProcessingException {
        final String urlRequest = String.format("https://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=%s&steamid=%s&relationship=all", apiKey, steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode friendslistNode = root.path("friendslist");
        JsonNode friendsNode = friendslistNode.path("friends");

        return friendsNode;
    }

    public JsonNode getWishlist(String steamId) throws JsonMappingException, JsonProcessingException {
        final String urlRequest = String.format("https://api.steampowered.com/IWishlistService/GetWishlist/v1/?steamid=%s", steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode response = root.path("response");
        JsonNode wishlist = response.path("items");

        return wishlist;
    }

    public JsonNode getAccountInfo(String steamID) throws JsonMappingException, JsonProcessingException {

        final String urlRequest = String.format("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=%s&steamids=%s",
                apiKey, steamID);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode accountInfo = root.path("response").path("players");

        return accountInfo;
    }

}
