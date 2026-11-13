
package com.example.demo.Service;

import com.example.demo.Model.GameRequirementsDTO;
import com.example.demo.Model.RequirementsSetDTO;
import com.example.demo.Util.FormatationComponentUtil;
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

    private static final Pattern ALTERNATIVE_SEPARATOR = Pattern.compile(
            "(?i)\\s+or\\s+|\\s*/\\s*|\\s*\\|\\s*|\\s*;\\s*"
                    + "|,\\s*(?=(?:nvidia|amd|ati|intel|geforce|"
                    + "radeon|gtx|rtx|gts)\\b)"
    );

    private static final Pattern CPU_MODEL = Pattern.compile(
            "(?i).*(?:"
                    + "intel\\s+(?:core|pentium|celeron|xeon)\\b"
                    + "|amd\\s+(?:fx|phenom|ryzen|athlon|sempron)\\b"
                    + "|core\\s+i[3579]\\b"
                    + "|pentium\\s+[a-z0-9-]+"
                    + "|celeron\\s+[a-z0-9-]+"
                    + "|xeon\\s+[a-z0-9-]+"
                    + "|ryzen\\s+[a-z0-9-]+"
                    + "|phenom\\s+[a-z0-9-]+"
                    + "|athlon\\s+[a-z0-9-]+"
                    + "|fx[- ]?\\d{3,}"
                    + ").*"
    );

    private static final Pattern GPU_MODEL = Pattern.compile(
            "(?i).*(?:"
                    + "nvidia\\b|geforce\\b|gtx\\b|rtx\\b|gts\\b"
                    + "|radeon\\b|ati\\s+"
                    + "|amd\\s+radeon\\b"
                    + "|intel\\s+(?:iris|uhd|hd\\s+graphics)\\b"
                    + ").*"
    );

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

        String cleanValue = value.replaceFirst(
                "(?i)" + Pattern.quote(label) + "\\s*:",
                ""
        ).trim();

        String[] alternatives = ALTERNATIVE_SEPARATOR.split(cleanValue);

        List<String> normalizedOptions = new java.util.ArrayList<>();

        String previousGpuManufacturer = null;

        for (String alternative : alternatives) {

            System.out.println("\n========== DEBUG PARSER ==========");
            System.out.println("Alternativa original: [" + alternative + "]");

            String cleaned = cleanHardwareString(alternative);

            System.out.println("Após cleanHardwareString: [" + cleaned + "]");

            if (cleaned.isBlank()) {
                continue;
            }

            if (label.equalsIgnoreCase("Processor")) {
                if (cleaned.matches("(?i)^core\\s+i[3579]\\b.*")) {
                    cleaned = "Intel " + cleaned;
                }

                System.out.println("Antes da validação: [" + cleaned + "]");

                if (isValidHardwareModel(cleaned, label)) {
                    String formatted = FormatationComponentUtil.formatCpuName(cleaned);

                    System.out.println("Resultado final: [" + formatted + "]");

                    normalizedOptions.add(formatted);
                }

            } else if (label.equalsIgnoreCase("Graphics")) {

                String lower = cleaned.toLowerCase();

                if (lower.matches("\\d{3,4}")) {
                    if (previousGpuManufacturer != null) {
                        cleaned = previousGpuManufacturer + " " + cleaned;
                    }
                }

                if (lower.contains("nvidia") || lower.contains("geforce")) {
                    previousGpuManufacturer = "geforce";

                    if (lower.matches(".*\\b\\d{3,4}\\b.*")
                            && !lower.contains("geforce")) {
                        cleaned = "geforce " + cleaned;
                    }
                } else if (lower.contains("ati") || lower.contains("radeon")) {
                    previousGpuManufacturer = "ati";
                }

                if (isValidHardwareModel(cleaned, label)) {
                    normalizedOptions.add(
                            FormatationComponentUtil.formatGpuName(cleaned)
                    );
                }
            }
        }

        return normalizedOptions.stream()
                .distinct()
                .toList();
    }


    private String cleanHardwareString(String value) {

        String cleaned = value;

        // Remove expressões genéricas que não representam modelos.
        cleaned = cleaned.replaceAll(
                "(?i)\\b(?:or\\s+better|and\\s+better|"
                        + "or\\s+equivalent|equivalent(?:\\s+processor)?)\\b",
                ""
        );

        // Remove frequências, por exemplo: 2.8 GHz.
        cleaned = cleaned.replaceAll(
                "(?i)\\b\\d+(?:\\.\\d+)?\\s*GHz\\b",
                ""
        );

        // Remove memória de vídeo, por exemplo: 2GB VRAM ou 512 MB.
        cleaned = cleaned.replaceAll(
                "(?i)\\b\\d+\\s*(?:GB|MB)\\s*"
                        + "(?:VRAM|video\\s+memory|graphics\\s+memory)?\\b",
                ""
        );

        // Remove conectivos que podem sobrar após remover especificações.
        cleaned = cleaned.replaceAll(
                "(?i)\\bwith\\b",
                ""
        );

        // Remove informações de Shader Model e DirectX.
        cleaned = cleaned.replaceAll(
                "(?i)\\bshader\\s+model\\s*[\\d.]+\\b",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?i)\\bDirectX\\s*\\d+(?:\\.\\d+)?\\b",
                ""
        );

        // Remove símbolos e espaços excedentes.
        cleaned = cleaned
                .replaceAll("[®™℠©]", "")
                .replaceAll("\\s+", " ")
                .replaceAll("^[\\s,;:/-]+|[\\s,;:/-]+$", "")
                .trim();

        return cleaned;
    }

    private boolean isValidHardwareModel(String option, String label) {
        if (label.equalsIgnoreCase("Processor")) {
            return CPU_MODEL.matcher(option).matches();
        }

        if (label.equalsIgnoreCase("Graphics")) {
            return GPU_MODEL.matcher(option).matches();
        }

        return false;
    }

    private Integer parseMemory(String value) {
        Matcher matcher = Pattern
                .compile("(\\d+)\\s*GB", Pattern.CASE_INSENSITIVE)
                .matcher(value);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    private Integer parseStorage(String value) {
        Matcher matcher = Pattern
                .compile("(\\d+)\\s*GB", Pattern.CASE_INSENSITIVE)
                .matcher(value);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }
}
