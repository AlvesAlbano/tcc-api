package com.example.demo;

import com.example.demo.Model.ComponentDTO;
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
import java.util.Comparator;
import java.util.List;

@Component
public class CompareCPU {

    private List<ComponentDTO> raspagemWeb(String url) throws IOException, InterruptedException {
        List<ComponentDTO> componenteList = new ArrayList<>();

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

        for(Element scoreBar: scoreBars){
            Element nomeElement = scoreBar.selectFirst(".score-bar-name");
            Element pontuacaoElement = scoreBar.selectFirst(".score-bar-result");

            String nome = nomeElement.text().trim();
            int pontuacao = Integer.parseInt(
                pontuacaoElement.text().trim()
            );

            ComponentDTO componente = new ComponentDTO(nome,null,"CPU",pontuacao);
            componenteList.add(componente);
        }

        return componenteList;
    }

    public boolean comparar(String cpu1,String cpu2) throws IOException, InterruptedException {
        final String URL = String.format("https://nanoreview.net/en/cpu-compare/%s-vs-%s?uc=gaming",cpu1,cpu2);

        System.out.printf("Url da comparação: %s\n",URL);

        final List<ComponentDTO> ComponentDTOList = raspagemWeb(URL);

//        System.out.println(ComponentDTOList);

        ComponentDTO componente_1 = ComponentDTOList.get(0);
        ComponentDTO componente_2 = ComponentDTOList.get(1);

        if (componente_1.score() > componente_2.score()){
            System.out.printf("%s é melhor que %s\n",componente_1.name(),componente_2.name());
            System.out.println("Emoji de certo");
        } else {
            System.out.printf("%s é melhor que %s\n",componente_2.name(),componente_1.name());
            System.out.println("Emoji de X");
        }
        return componente_1.score() > componente_2.score();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CompareCPU compareCPU = new CompareCPU();
        System.out.println(compareCPU.comparar("amd-ryzen-5-2400g","amd-ryzen-5-3600x"));
    }
}
