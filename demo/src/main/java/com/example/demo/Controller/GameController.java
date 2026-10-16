package com.example.demo.Controller;

import com.example.demo.Model.GameRequirementsDTO;
import com.example.demo.Service.GameService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
@Tag(name="Games")
public class GameController {

//    private final SteamClient steamClient;
//    private final GameCatalogService gameCatalogService;
//
//    public GameController(SteamClient steamClient, GameCatalogService gameCatalogService) {
//        this.steamClient = steamClient;
//        this.gameCatalogService = gameCatalogService;
//    }

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{appId}")
    @Operation(summary = "Retorna informações detalhadas de um jogo",description = "(Ex: 3050900)")
    public JsonNode getGameInfo(@PathVariable int appId) throws JsonProcessingException {
//        return steamClient.getGameInfo(appId);
        return gameService.getGameInfoJson(appId);
    }

    @GetMapping("requirements/{appId}")
    @Operation(summary = "Retorna os requisitos minimos e recomendados de um jogo",description = "(Ex: 3050900)")
    public GameRequirementsDTO getGameRequirementsInfo(@PathVariable int appId) throws JsonProcessingException {

        return gameService.getGameRequirements(appId);
    }
}