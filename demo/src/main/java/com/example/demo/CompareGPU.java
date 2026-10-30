package com.example.demo;

import com.example.demo.Model.ComponentDTO;
import com.example.demo.Model.ComponentScrapperDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CompareGPU {

    private List<ComponentScrapperDTO> raspagemWeb(String url) throws IOException, InterruptedException {
        List<ComponentScrapperDTO> componenteList = new ArrayList<>();

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "*/*")
                .header("User-Agent", "Thunder Client (https://www.thunderclient.com)")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        final String html = response.body();

        Document soup = Jsoup.parse(html);

        Element divGaming = soup.selectFirst("div.two-columns-item.mb");
        Elements scoreBars = divGaming.select(".score-bar");

        for (Element scoreBar : scoreBars) {
            Element nomeElement = scoreBar.selectFirst(".score-bar-name");
            Element pontuacaoElement = scoreBar.selectFirst(".score-bar-result");

            String nome = nomeElement.text().trim();
            int pontuacao = Integer.parseInt(
                    pontuacaoElement.text().trim()
            );

            ComponentScrapperDTO component = new ComponentScrapperDTO(nome, pontuacao);
            componenteList.add(component);
        }

        return componenteList;
    }

    public boolean comparar(ComponentDTO gpuUser, ComponentDTO gpuGame) throws IOException, InterruptedException {

//        final String gpuUserUrl = formatGpuString(gpuUser);
//        final String gpuGameUrl = formatGpuString(gpuGame);

//        final String URL = String.format("https://nanoreview.net/en/gpu-compare/%s-vs-%s",gpuUserUrl,gpuGameUrl);

        final String URL = String.format("https://nanoreview.net/en/gpu-compare/%s-vs-%s", gpuUser.name(), gpuGame.name());

        int gpuUserScore;
        int gpuGameScore;

        System.out.printf("Url da comparação: %s\n", URL);
        final List<ComponentScrapperDTO> ComponentScrapperDTOList = raspagemWeb(URL);

        if (isUserGpu(gpuUser, ComponentScrapperDTOList.getFirst())) {
            gpuUserScore = ComponentScrapperDTOList.getFirst().score();
            gpuGameScore = ComponentScrapperDTOList.getLast().score();
        } else {
            gpuUserScore = ComponentScrapperDTOList.getLast().score();
            gpuGameScore = ComponentScrapperDTOList.getFirst().score();
        }

        if (gpuUserScore > gpuGameScore) {
            System.out.printf("%s é melhor que %s\n", gpuUser.name(), gpuGame.name());
            System.out.println("Gpu do usuario melhor que a do jogo");
            System.out.println("Emoji de certo");
        } else {
            System.out.printf("%s é melhor que %s\n", gpuGame.name(), gpuUser.name());
            System.out.println("Gpu do usuario não é melhor que a do jogo");
            System.out.println("Emoji de X");
        }

        return gpuUserScore > gpuGameScore;
    }

    private boolean isUserGpu(ComponentDTO componentDTO, ComponentScrapperDTO componentScrapperDTO) throws IOException, InterruptedException {

        String gpuScapperName = componentScrapperDTO.name();

        gpuScapperName = gpuScapperName.replace(" ", "-").toLowerCase();

        return componentDTO.name().equals(gpuScapperName) && componentDTO.owner().equals("user");
    }
}