package com.example.demo.Service;


import com.example.demo.Client.SteamClient;
import com.example.demo.Model.GameRequirementsDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final SteamRequirementsParser steamRequirementsParser;
    private final SteamClient steamClient;

    public GameService(SteamRequirementsParser steamRequirementsParser, SteamClient steamClient) {
        this.steamRequirementsParser = steamRequirementsParser;
        this.steamClient = steamClient;
    }

    public JsonNode getGameInfoJson(int appId) throws JsonProcessingException {
        return steamClient.getGameInfo(appId);
    }

    private JsonNode getGameRequirementsJson(int appId) throws JsonProcessingException {
        JsonNode jsonGameInfo = getGameInfoJson(appId);
        return jsonGameInfo == null ? null : jsonGameInfo.path("pc_requirements");
    }

    public GameRequirementsDTO getGameRequirements(int appId) throws JsonProcessingException {
        JsonNode jsonRequirements = getGameRequirementsJson(appId);

        if (jsonRequirements == null || jsonRequirements.isMissingNode() || jsonRequirements.isNull()) {
            return steamRequirementsParser.parse(null, null);
        }

        String minimumHtml = jsonRequirements.get("minimum").asText("");
        String recommendedHtml = jsonRequirements.get("recommended").asText("");

        return steamRequirementsParser.parse(minimumHtml, recommendedHtml);
    }
}