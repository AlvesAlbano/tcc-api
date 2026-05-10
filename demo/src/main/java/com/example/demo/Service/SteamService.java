package com.example.demo.Service;

import com.example.demo.Client.SteamClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SteamService {

    @Autowired
    private SteamClient steamClient;

    public JsonNode getListaJogos(String chaveApi, String nomeUsuario) throws JsonProcessingException {

        final String STEAM_ID = steamClient.getSteamID(chaveApi, nomeUsuario);

        return steamClient.getJogos(chaveApi, STEAM_ID);
    }

    public JsonNode getInfoJogo(int appId) throws JsonMappingException, JsonProcessingException {
        return steamClient.getInfoJogo(appId);
    }

    public JsonNode getListaDesejo(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        final String STEAM_ID = steamClient.getSteamID(chaveApi, nomeUsuario);

        return steamClient.getListaDesejo(STEAM_ID);
    }

    public JsonNode getContasAdicionadas(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        // final String STEAM_ID = steamClient.getSteamID(chaveApi, nomeUsuario);
        final String STEAM_ID = "76561197960435530";

        return steamClient.getContasAdicionadas(chaveApi, STEAM_ID);
    }

    public JsonNode getInfoConta(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {

        final String STEAM_ID = steamClient.getSteamID(chaveApi, nomeUsuario);
        return steamClient.getInfoConta(chaveApi, STEAM_ID);
    }

    public int getQtdJogosConta(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        
        final String STEAM_ID = steamClient.getSteamID(chaveApi, nomeUsuario);
        
        return steamClient.getQtdJogosConta(chaveApi, STEAM_ID);
    }

    public int getQtdJogosDesejo(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        final String STEAM_ID = steamClient.getSteamID(chaveApi, nomeUsuario);

        return steamClient.getQtdJogosDesejo(STEAM_ID);
    }

    public List<JsonNode> jogosRecemJogados(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        final String STEAM_ID = steamClient.getSteamID(chaveApi, nomeUsuario);

        return steamClient.jogosRecemJogados(chaveApi,STEAM_ID);
    }
}
