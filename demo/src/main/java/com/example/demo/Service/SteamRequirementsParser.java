package com.example.demo.Service;

import com.example.demo.Model.GameRequirementsDTO;
import com.example.demo.Model.RequirementsSetDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SteamRequirementsParser {

    public GameRequirementsDTO parse(
            String minimumHtml,
            String recommendedHtml
    ) {
        RequirementsSetDTO minimum = parseRequirementsSet(minimumHtml);
        RequirementsSetDTO recommended = parseRequirementsSet(recommendedHtml);

        return new GameRequirementsDTO(minimum, recommended);
    }

    private RequirementsSetDTO parseRequirementsSet(String html) {
        if (html == null || html.isBlank()) {
            return new RequirementsSetDTO(
                    List.of(),
                    List.of(),
                    null,
                    null
            );
        }

        Document document = Jsoup.parse(html);

        List<String> cpuOptions = List.of();
        List<String> gpuOptions = List.of();

        Integer ramGb = null;
        Integer storageGb = null;

        Elements items = document.select("li");

        for (Element item : items) {
            Element strong = item.selectFirst("strong");

            if (strong == null) {
                continue;
            }

            String label = strong.text().replace(":", "").trim();

            String value = item.text();

            if (label.equalsIgnoreCase("Processor")) {
                cpuOptions = parseAlternatives(value, label);

            } else if (label.equalsIgnoreCase("Graphics")) {
                gpuOptions = parseAlternatives(value, label);

            } else if (label.equalsIgnoreCase("Memory")) {
                ramGb = parseMemory(value);

            } else if (label.equalsIgnoreCase("Storage")) {
                storageGb = parseStorage(value);
            }
        }

        return new RequirementsSetDTO(
                cpuOptions,
                gpuOptions,
                ramGb,
                storageGb
        );
    }

    private List<String> parseAlternatives(String value, String label) {
        String cleanValue = value
                .replaceFirst(
                        "(?i)" + Pattern.quote(label) + "\\s*:",
                        ""
                )
                .trim();

        return Arrays.stream(
                        cleanValue.split("(?i)\\s+or\\s+")
                )
                .map(String::trim)
                .filter(option -> !option.isBlank())
                .toList();
    }

    private Integer parseMemory(String value) {
        Matcher matcher = Pattern
                .compile(
                        "(\\d+)\\s*GB",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(value);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    private Integer parseStorage(String value) {
        Matcher matcher = Pattern
                .compile(
                        "(\\d+)\\s*GB",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(value);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

}
