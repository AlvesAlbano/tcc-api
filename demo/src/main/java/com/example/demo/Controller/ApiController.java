package com.example.demo.Controller;

import com.example.demo.Service.SteamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/projeto")
public class ApiController {

    @Autowired
    private SteamService steamService;

    @GetMapping("lista-jogos/{nomeUsuario}")
    @Operation(description = "chaveApi:E1395357FD05E557F221B8D694389B79 nomeUsuario: motorfonte")
    public JsonNode getJogos(@RequestParam("chaveApi") String chaveApi, @PathVariable("nomeUsuario") String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        return steamService.getListaJogos(chaveApi, nomeUsuario);
    }

    @GetMapping("info-jogo/{appId}")
    @Operation(description = "App ID de um jogo:3050900")
    public JsonNode getLojaInfo(@PathVariable("appId") int appId) throws JsonMappingException, JsonProcessingException {
        return steamService.getInfoJogo(appId);
    }

    @GetMapping("lista-contas-adicionadas/{nomeUsuario}")
    @Operation(description = "chaveApi:E1395357FD05E557F221B8D694389B79 nomeUsuario: motorfonte. To usando outro steamID pq a lista de contas adicionadas do motorfonte é privada")
    public JsonNode getListaAmigos(@RequestParam("chaveApi") String chaveApi, @PathVariable("nomeUsuario") String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        return steamService.getContasAdicionadas(chaveApi, nomeUsuario);
    }

    @GetMapping("lista-desejos/{nomeUsuario}")
    @Operation(description = "chaveApi:E1395357FD05E557F221B8D694389B79 nomeUsuario: motorfonte")
    public JsonNode getListaDesejos(@RequestParam("chaveApi") String chaveApi, @PathVariable("nomeUsuario") String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        return steamService.getListaDesejo(chaveApi, nomeUsuario);
    }

    @GetMapping("info-conta/{nomeUsuario}")
    @Operation(description = "chaveApi:E1395357FD05E557F221B8D694389B79 nomeUsuario: motorfonte")
    public JsonNode getInfoConta(@RequestParam("chaveApi") String chaveApi, @PathVariable("nomeUsuario") String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        return steamService.getInfoConta(chaveApi, nomeUsuario);
    }

    @GetMapping("quantidade-jogos-conta/{nomeUsuario}")
    @Operation(description = "chaveApi:E1395357FD05E557F221B8D694389B79 nomeUsuario: motorfonte")
    public int getQtdJogosConta(@RequestParam("chaveApi") String chaveApi, @PathVariable("nomeUsuario") String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        return steamService.getQtdJogosConta(chaveApi, nomeUsuario);
    }

    @GetMapping("quantidade-jogos-desejo/{nomeUsuario}")
    @Operation(description = "chaveApi:E1395357FD05E557F221B8D694389B79 nomeUsuario: motorfonte")
    public int getQtdJogosDesejo(@RequestParam("chaveApi") String chaveApi, @PathVariable("nomeUsuario") String nomeUsuario) throws JsonMappingException, JsonProcessingException {
        return steamService.getQtdJogosDesejo(chaveApi, nomeUsuario);
    }

    @GetMapping("jogos-recem-jogados/{nomeUsuario}")
    @Operation(description = "chaveApi:E1395357FD05E557F221B8D694389B79 nomeUsuario: motorfonte")
    public List<JsonNode> jogosRecemJogados(String chaveApi, String nomeUsuario) throws JsonMappingException, JsonProcessingException {

        return steamService.jogosRecemJogados(chaveApi,nomeUsuario);
    }
}