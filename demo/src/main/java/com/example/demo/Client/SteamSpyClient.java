package com.example.demo.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SteamSpyClient {

    private final RestTemplate restTemplate =
            new RestTemplate();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public JsonNode getGamesByTag(String tag)
            throws Exception {

        String url =
                "https://steamspy.com/api.php?request=tag&tag=" + tag;

        String json =
                restTemplate.getForObject(
                        url,
                        String.class
                );

        return objectMapper.readTree(json);
    }

    public JsonNode getGameDetails(int appId)
            throws Exception {

        String url =
                "https://steamspy.com/api.php?request=appdetails&appid="
                        + appId;

        String json =
                restTemplate.getForObject(
                        url,
                        String.class
                );

        return objectMapper.readTree(json);
    }
}