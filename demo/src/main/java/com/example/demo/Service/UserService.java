package com.example.demo.Service;

import com.example.demo.Client.SteamClient;
import com.example.demo.Model.GameDTO;
import com.example.demo.Model.UserProfileDTO;
import com.example.demo.Model.UserProfileHeaderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final SteamClient steamClient;
    private final ProfileService profileService;

    public UserService(SteamClient steamClient, ProfileService profileService) {
        this.steamClient = steamClient;
        this.profileService = profileService;
    }

    public UserProfileHeaderDTO getProfileHeader(String user) throws Exception {
        String steamId = steamClient.resolveSteamId(user);
        JsonNode account = steamClient.getAccountInfo(steamId);

        if (account == null || account.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado ou privado");
        }

        JsonNode player = account.get(0);

        JsonNode games = steamClient.getGames(steamId);
        JsonNode friends = steamClient.getFriends(steamId);
        JsonNode wishlist = steamClient.getWishlist(steamId);

        UserProfileDTO profile = profileService.buildProfile(steamId);

        List<String> favoriteGenres = profile.tagWeights() != null
                ? profile.tagWeights()
                .keySet()
                .stream()
                .limit(3)
                .toList()
                : List.of();

        return new UserProfileHeaderDTO(
                player.path("personaname").asText(),
                player.path("avatarfull").asText(),
                games != null ? games.size() : 0,
                friends != null ? friends.size() : 0,
                wishlist != null ? wishlist.size() : 0,
                favoriteGenres
        );
    }

    public JsonNode getUserInfo(String user) throws JsonProcessingException {
        String steamId = steamClient.resolveSteamId(user);
        return steamClient.getAccountInfo(steamId);
    }

    public List<GameDTO> getOwnedGames(String user) throws JsonProcessingException {
        String steamId = steamClient.resolveSteamId(user);
        return steamClient.getGamesTyped(steamId);
    }

    public JsonNode getFriends(String user) throws JsonProcessingException {
        String steamId = steamClient.resolveSteamId(user);
        return steamClient.getFriends(steamId);
    }

    public JsonNode getWishlist(String user) throws JsonProcessingException {
        String steamId = steamClient.resolveSteamId(user);
        return steamClient.getWishlist(steamId);
    }
}