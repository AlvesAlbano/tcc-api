package com.example.demo;

import com.example.demo.Model.ComponentJsonDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class ScrapperGPU {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ScrapperGPU() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<ComponentJsonDTO> gpusDisponiveis() throws IOException, InterruptedException {
        final String URL = "https://nanoreview.net/api/search?q=*&limit=500&type=gpu";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Accept", "*/*")
                .header("User-Agent", "Thunder Client (https://www.thunderclient.com)")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        final String jsonGpu = response.body();

        return objectMapper.readValue(
                jsonGpu,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ComponentJsonDTO.class)
        );
    }

    public List<ComponentJsonDTO> dynamicSearch(String gpuName) throws IOException, InterruptedException {
        final String URL = String.format("https://nanoreview.net/api/search?q=%s&limit=10&type=gpu",gpuName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Accept", "*/*")
                .header("User-Agent", "Thunder Client (https://www.thunderclient.com)")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        final String jsonGpu = response.body();

        return objectMapper.readValue(
                jsonGpu,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ComponentJsonDTO.class)
        );
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ScrapperGPU scrapperGPU = new ScrapperGPU();

        System.out.println(scrapperGPU.gpusDisponiveis());
    }
}
