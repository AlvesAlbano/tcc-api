package com.example.demo.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class SteamClient {

    @Autowired
    private ObjectMapper objectMapper;

    // private final WebClient webClient = WebClient.create();
    private final RestTemplate restTemplate = new RestTemplate();

    public SteamClient() {

    }

    public String getSteamID(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {

        final String urlModelo = String.format(
                "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=%s&vanityurl=%s", chaveApi,
                nomeUsuario);

        String json = restTemplate.getForObject(urlModelo, String.class);
        JsonNode root = objectMapper.readTree(json);

        String steamID = root.path("reponse").path("steamid").asText();

        return steamID;
    }

    public JsonNode getJogos(String chaveApi, String steamId) throws JsonMappingException, JsonProcessingException {

        // final String urlRequest = String.format(
        //         "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=%s&steamid=%s&format=json",
        //         chaveApi, steamId);
        final String urlRequest = String.format("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=true", chaveApi, steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);
        JsonNode root = objectMapper.readTree(json);

        JsonNode listaJogos = root.path("response").path("games");

        return listaJogos;
    }

    public JsonNode getInfoJogo(int appId) throws JsonMappingException, JsonProcessingException {
        final String urlRequest = String.format("https://store.steampowered.com/api/appdetails?appids=%d", appId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.path(String.valueOf(appId)).path("data");

        return dataNode;
    }

    public JsonNode getContasAdicionadas(String chaveApi, String steamId) throws JsonMappingException, JsonProcessingException {
        final String urlRequest = String.format("https://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=%s&steamid=%s&relationship=all", chaveApi, steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode friendslistNode = root.path("friendslist");
        JsonNode friendsNode = friendslistNode.path("friends");

        return friendsNode;
    }

    public JsonNode getListaDesejo(String steamId) throws JsonMappingException, JsonProcessingException {
        final String urlRequest = String.format("https://api.steampowered.com/IWishlistService/GetWishlist/v1/?steamid=%s", steamId);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode listaDesejos = root.path("response").path("items");

        return listaDesejos;
    }

    public JsonNode getInfoConta(String chaveApi, String steamID) throws JsonMappingException, JsonProcessingException {

        final String urlRequest = String.format("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=%s&steamids=%s", chaveApi, steamID);

        String json = restTemplate.getForObject(urlRequest, String.class);

        JsonNode root = objectMapper.readTree(json);

        JsonNode infoConta = root.path("response").path("players");

        return infoConta;
    }

    public int getQtdJogosConta(String chaveApi, String steamID) throws JsonMappingException, JsonProcessingException {
        
        return getJogos(chaveApi,steamID).size();
    }

    public int getQtdJogosDesejo(String steamID) throws JsonMappingException, JsonProcessingException {
        
        return getListaDesejo(steamID).size();
    }

    public List<JsonNode> jogosRecemJogados(String chaveApi, String steamID) throws JsonMappingException, JsonProcessingException {
        JsonNode jsonJogos = getJogos(chaveApi,steamID);
        List<JsonNode> jogosRecemJogados = new ArrayList<>();

        for(JsonNode jogo: jsonJogos){
            if (jogo.hasNonNull("playtime_2weeks")){
                jogosRecemJogados.add(jogo);
            }
        }
        return jogosRecemJogados;
    }
}
