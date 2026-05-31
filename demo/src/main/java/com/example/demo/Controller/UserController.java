package com.example.demo.Controller;

import com.example.demo.Client.SteamClient;
import com.example.demo.Model.GameDTO;
import com.example.demo.Model.UserProfileHeaderDTO;
import com.example.demo.Service.UserService;
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

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{username}")
    @Operation(summary = "Pega resumo do perfil")
    public UserProfileHeaderDTO getProfileHeader(@PathVariable String username) throws Exception {
        return userService.getProfileHeader(username);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Pega informações do usuário (Ex: motorfonte)")
    public JsonNode getUserInfo(@PathVariable String username) throws JsonProcessingException {
        return userService.getUserInfo(username);
    }

    @GetMapping("/{username}/games")
    @Operation(summary = "Pega a lista de jogos do usuário (Ex: motorfonte)")
    public List<GameDTO> getOwnedGames(@PathVariable String username) throws JsonProcessingException {
        return userService.getOwnedGames(username);
    }

    @GetMapping("/{username}/friends")
    @Operation(summary = "Pega os amigos do usuário (Ex: motorfonte)")
    public JsonNode getFriendList(@PathVariable String username) throws JsonProcessingException {
        return userService.getFriends(username);
    }

    @GetMapping("/{username}/wishlist")
    @Operation(summary = "Pega os jogos na lista de desejo do usuário (Ex: motorfonte)")
    public JsonNode getWishlist(@PathVariable String username) throws JsonProcessingException {
        return userService.getWishlist(username);
    }

}
