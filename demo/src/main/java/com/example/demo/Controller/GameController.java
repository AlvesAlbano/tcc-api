package com.example.demo.Controller;

import com.example.demo.Client.SteamClient;
import com.example.demo.Entity.Game;
import com.example.demo.Service.GameCatalogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@Tag(name="Games")
public class GameController {

    private final SteamClient steamClient;
    private final GameCatalogService gameCatalogService;

    public GameController(SteamClient steamClient, GameCatalogService gameCatalogService) {
        this.steamClient = steamClient;
        this.gameCatalogService = gameCatalogService;
    }

    @GetMapping("/{appId}")
    @Operation(summary = "Retorna informações detalhadas de um jogo (Ex: 3050900)")
    public JsonNode getGameInfo(@PathVariable int appId) throws JsonProcessingException {
        return steamClient.getGameInfo(appId);
    }



}
