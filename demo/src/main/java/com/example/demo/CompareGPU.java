package com.example.demo;

import com.example.demo.Model.ComponentDTO;
import com.example.demo.Model.ComponentJsonDTO;
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
public class CompareGPU {

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

        final String html = response.body();

        Document soup = Jsoup.parse(html);

        Element divGaming = soup.selectFirst("div.two-columns-item.mb");
        Elements scoreBars = divGaming.select(".score-bar");

        for(Element scoreBar: scoreBars){
            Element nomeElement = scoreBar.selectFirst(".score-bar-name");
            Element pontuacaoElement = scoreBar.selectFirst(".score-bar-result");

            String nome = nomeElement.text().trim();
            int pontuacao = Integer.parseInt(
                pontuacaoElement.text().trim()
            );

            ComponentDTO componente = new ComponentDTO(nome,"","GPU",pontuacao);
            componenteList.add(componente);
        }

        return componenteList;
    }

    public boolean comparar(String gpuUser,String gpuGame) throws IOException, InterruptedException {

//        final String gpuUserUrl = formatGpuString(gpuUser);
//        final String gpuGameUrl = formatGpuString(gpuGame);

//        final String URL = String.format("https://nanoreview.net/en/gpu-compare/%s-vs-%s",gpuUserUrl,gpuGameUrl);

        final String URL = String.format("https://nanoreview.net/en/gpu-compare/%s-vs-%s",gpuUser,gpuGame);

        System.out.printf("Url da comparação: %s\n",URL);
        final List<ComponentDTO> ComponentDTOList = raspagemWeb(URL);

        ComponentDTO componente_1 = ComponentDTOList.get(0);
        ComponentDTO componente_2 = ComponentDTOList.get(1);

        System.out.println(ComponentDTOList);
//        labelGpu(gpuUser,"fds");
//        labelGpu(gpuUserUrl,"fds");

        if (componente_1.score() > componente_2.score()){
            System.out.printf("%s é melhor que %s\n",componente_1.name(),componente_2.name());
            System.out.println("Emoji de certo");
        } else {
            System.out.printf("%s é melhor que %s\n",componente_2.name(),componente_1.name());
            System.out.println("Emoji de X");
        }

        return componente_1.score() > componente_2.score();
    }

    private void labelGpu(String gpu,String owner) throws IOException, InterruptedException {
        ScrapperGPU scrapperGPU = new ScrapperGPU();
        List<ComponentJsonDTO> gpusDisponiveis = scrapperGPU.gpusDisponiveis();

        for (ComponentJsonDTO componentJsonDTO : gpusDisponiveis) {
            if(componentJsonDTO.slug().equals(gpu)){
                System.out.println(componentJsonDTO);
            }
        }

    }
    public static void main(String[] args) throws IOException, InterruptedException {
        CompareGPU compareGPU = new CompareGPU();
        compareGPU.comparar("apple-m5-gpu-10-core","adreno-x2-90");
    }
}
