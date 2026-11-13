
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class CompareGPU {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private List<ComponentScrapperDTO> raspagemWeb(String url)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "text/html")
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36")
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("NanoReview retornou HTTP " + response.statusCode());
        }

        String html = response.body();

        if (html == null || html.isBlank()) {
            throw new IOException("NanoReview retornou uma página vazia.");
        }

        Document soup = Jsoup.parse(html);

        Element container = soup.selectFirst("div.two-columns-item.mb");

        if (container == null) {
            container = soup.selectFirst("div.compare-tags-bars-highlight");
        }

        Elements scoreBars;

        if (container != null) {
            scoreBars = container.select(".score-bar");
        } else {
            scoreBars = soup.select(".score-bar");
        }

        if (scoreBars.size() < 2) {
            throw new IOException("Não foram encontradas duas pontuações de GPU na página do NanoReview.");
        }

        List<ComponentScrapperDTO> components = new ArrayList<>();

        for (Element scoreBar : scoreBars) {
            Element nameElement = scoreBar.selectFirst(".score-bar-name");
            Element scoreElement = scoreBar.selectFirst(".score-bar-result");

            if (nameElement == null || scoreElement == null) {
                continue;
            }

            String name = nameElement.text().trim();
            String scoreText = scoreElement.text().trim();

            try {
                int score = Integer.parseInt(scoreText);

                components.add(new ComponentScrapperDTO(name, score));

            } catch (NumberFormatException exception) {
                //
            }
        }

        if (components.size() < 2) {
            throw new IOException("As pontuações de GPU retornadas pelo NanoReview são inválidas.");
        }

        return components;
    }

    public boolean comparar(
            ComponentDTO gpuUser,
            ComponentDTO gpuGame
    ) throws IOException, InterruptedException {

        String url = String.format(
                "https://nanoreview.net/en/gpu-compare/%s-vs-%s",
                gpuUser.name(),
                gpuGame.name()
        );

        System.out.printf("URL da comparação: %s%n", url);

        List<ComponentScrapperDTO> components = raspagemWeb(url);

        ComponentScrapperDTO first = components.getFirst();
        ComponentScrapperDTO last = components.getLast();

        boolean firstIsUser = matchesName(gpuUser.name(), first.name());
        boolean lastIsUser = matchesName(gpuUser.name(), last.name());

        if (firstIsUser == lastIsUser) {
            throw new IOException(
                    "Não foi possível identificar a GPU do usuário no resultado do NanoReview."
            );
        }

        int userScore = firstIsUser
                ? first.score()
                : last.score();

        int gameScore = firstIsUser
                ? last.score()
                : first.score();

        System.out.printf(
                "GPU usuário: %s (%d) | GPU jogo: %s (%d)%n",
                gpuUser.name(),
                userScore,
                gpuGame.name(),
                gameScore
        );

        return userScore >= gameScore;
    }

    private boolean matchesName(String expected, String actual) {

        if (expected == null || actual == null) {
            return false;
        }

        String normalizedExpected = normalizeName(expected);
        String normalizedActual = normalizeName(actual);

        if (normalizedExpected.isBlank() || normalizedActual.isBlank()) {
            return false;
        }

        return normalizedExpected.equals(normalizedActual)
                || normalizedExpected.contains(normalizedActual)
                || normalizedActual.contains(normalizedExpected);
    }

    private String normalizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}
