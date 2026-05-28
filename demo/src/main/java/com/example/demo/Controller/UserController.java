package com.example.demo.Controller;

import com.example.demo.Client.SteamClient;
import com.example.demo.Model.GameDTO;
import com.example.demo.Service.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name="Users")
public class UserController {

    private final SteamClient steamClient;

    public UserController(SteamClient steamClient) {
        this.steamClient = steamClient;
    }

    @GetMapping("/{username}")
    @Operation(summary = "Pega informações do usuário (Ex: motorfonte)")
    public JsonNode getUserInfo(@PathVariable String steamId) throws JsonProcessingException {
        return steamClient.getAccountInfo(steamId);
    }

    @GetMapping("/{username}/games")
    @Operation(summary = "Pega a lista de jogos do usuário (Ex: motorfonte)")
    public List<GameDTO> getOwnedGames(@PathVariable String steamId) throws JsonProcessingException {
        return steamClient.getGamesTyped(steamId);
    }

    @GetMapping("/{username}/friends")
    @Operation(summary = "Pega os amigos do usuário (Ex: motorfonte)")
    public JsonNode getFriendList(@PathVariable String steamId) throws JsonProcessingException {
        return steamClient.getFriends(steamId);
    }

    @GetMapping("/{username}/wishlist")
    @Operation(summary = "Pega os jogos na lista de desejo do usuário (Ex: motorfonte)")
    public JsonNode getWishlist(@PathVariable String steamId) throws JsonProcessingException {
        return steamClient.getWishlist(steamId);
    }

}
