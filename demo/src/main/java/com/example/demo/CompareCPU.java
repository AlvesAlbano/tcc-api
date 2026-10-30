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
public class CompareCPU {

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


//        System.out.println("Status: " + response.statusCode());
//        System.out.println("Tamanho: " + response.body().length());
//        System.out.println("Tem elemento: " +
//                response.body().contains("compare-tags-bars-highlight"));

        final String html = response.body();

        Document soup = Jsoup.parse(html);

        Element divGaming = soup.selectFirst("div.compare-tags-bars-highlight");
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

    public boolean comparar(ComponentDTO cpuUser, ComponentDTO cpuGame) throws IOException, InterruptedException {
        final String URL = String.format("https://nanoreview.net/en/cpu-compare/%s-vs-%s?uc=gaming", cpuUser.name(), cpuGame.name());

        int cpuUserScore;
        int cpuGameScore;

        System.out.printf("Url da comparação: %s\n", URL);
        final List<ComponentScrapperDTO> ComponentScrapperDTOList = raspagemWeb(URL);

        if (isUserCpu(cpuUser, ComponentScrapperDTOList.getFirst())) {
            cpuUserScore = ComponentScrapperDTOList.getFirst().score();
            cpuGameScore = ComponentScrapperDTOList.getLast().score();
        } else {
            cpuUserScore = ComponentScrapperDTOList.getLast().score();
            cpuGameScore = ComponentScrapperDTOList.getFirst().score();
        }

        if (cpuUserScore > cpuGameScore) {
            System.out.printf("%s é melhor que %s\n", cpuUser.name(), cpuGame.name());
            System.out.println("Cpu do usuario melhor que a do jogo");
            System.out.println("Emoji de certo");
        } else {
            System.out.printf("%s é melhor que %s\n", cpuGame.name(), cpuUser.name());
            System.out.println("cpu do usuario não é melhor que a do jogo");
            System.out.println("Emoji de X");
        }

        return cpuUserScore > cpuGameScore;
    }

    private boolean isUserCpu(ComponentDTO componentDTO, ComponentScrapperDTO componentScrapperDTO) throws IOException, InterruptedException {

        String gpuScapperName = componentScrapperDTO.name();

        gpuScapperName = gpuScapperName.replace(" ", "-").toLowerCase();

        return componentDTO.name().equals(gpuScapperName) && componentDTO.owner().equals("user");
    }
}