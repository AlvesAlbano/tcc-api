package com.example.demo;

import com.example.demo.Model.ComponentJsonDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class ScrapperCPU {

    private final String URL = "https://nanoreview.net/api/search?q=*&limit=500&type=cpu";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ScrapperCPU() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<ComponentJsonDTO> cpusDisponiveis() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Accept", "*/*")
                .header("User-Agent", "Thunder Client (https://www.thunderclient.com)")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        final String jsonCpu = response.body();

        return objectMapper.readValue(
                jsonCpu,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ComponentJsonDTO.class)
        );
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ScrapperCPU scrapperCPU = new ScrapperCPU();

        System.out.println(scrapperCPU.cpusDisponiveis());
    }
}
