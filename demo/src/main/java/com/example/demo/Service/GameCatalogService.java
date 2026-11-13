package com.example.demo.Service;

import com.example.demo.Client.SteamClient;
import com.example.demo.Client.SteamSpyClient;
import com.example.demo.Entity.Game;
import com.example.demo.Entity.GameRequirement;
import com.example.demo.Entity.RequirementType;
import com.example.demo.Entity.Tag;
import com.example.demo.Model.GameRequirementsDTO;
import com.example.demo.Model.RequirementsSetDTO;
import com.example.demo.Repository.GameRepository;
import com.example.demo.Repository.GameRequirementRepository;
import com.example.demo.Repository.TagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GameCatalogService {

    private final GameRepository gameRepository;
    private final GameRequirementRepository requirementRepository;
    private final TagRepository tagRepository;
    private final SteamClient steamClient;
    private final SteamSpyClient steamSpyClient;
    private final SteamRequirementsParser requirementsParser;
    private final ObjectMapper objectMapper;

    public GameCatalogService(
            GameRepository gameRepository,
            GameRequirementRepository requirementRepository,
            TagRepository tagRepository,
            SteamClient steamClient,
            SteamSpyClient steamSpyClient,
            SteamRequirementsParser requirementsParser,
            ObjectMapper objectMapper
    ) {
        this.gameRepository = gameRepository;
        this.requirementRepository = requirementRepository;
        this.tagRepository = tagRepository;
        this.steamClient = steamClient;
        this.steamSpyClient = steamSpyClient;
        this.requirementsParser = requirementsParser;
        this.objectMapper = objectMapper;
    }

    public void importGamesByTag(String tagName, int limit) throws Exception {
        JsonNode gamesNode = steamSpyClient.getGamesByTag(tagName);
        Iterator<String> gameIds = gamesNode.fieldNames();
        int processed = 0;

        while (gameIds.hasNext() && processed < limit) {
            int appId;
            try {
                appId = Integer.parseInt(gameIds.next());
            } catch (NumberFormatException ex) {
                continue;
            }

            try {
                if (importOrUpdateGame(appId)) {
                    processed++;
                }
            } catch (Exception ex) {
                System.err.println("Falha ao importar/atualizar jogo " + appId + ": " + ex.getMessage());
            }
        }
        System.out.println("Importação/atualização finalizada. Jogos processados: " + processed);
    }

    @Transactional
    public boolean importOrUpdateGame(int appId) throws Exception {
        JsonNode info = steamClient.getGameInfo(appId);
        waitBetweenRequests();

        if (info == null || info.isMissingNode() || info.isEmpty()) {
            return false;
        }

        String name = info.path("name").asText("").trim();

        if (name.isBlank()) {
            return false;
        }

        Game game = gameRepository.findById(appId).orElseGet(Game::new);
        game.setAppId(appId);
        game.setName(name);
        game.setImageUrl(nullableText(info.path("header_image").asText(null)));
        game.setShortDescription(nullableText(info.path("short_description").asText(null)));
        game.setReleaseYear(parseReleaseYear(info.path("release_date").path("date").asText(null)));

        JsonNode spyDetails = steamSpyClient.getGameDetails(appId);
        waitBetweenRequests();

        JsonNode tagsNode = spyDetails.path("tags");
        Set<Tag> gameTags = new HashSet<>();
        boolean receivedTags = tagsNode.isObject() && tagsNode.fieldNames().hasNext();

        if (receivedTags) {
            Iterator<String> names = tagsNode.fieldNames();
            while (names.hasNext()) {
                String raw = names.next().trim();
                if (raw.isBlank()) {
                    continue;
                }

                String normalized = raw.replaceAll("\\s+", " ");

                Tag tag = tagRepository.findByName(normalized).orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(normalized);
                    return tagRepository.save(newTag);
                });

                gameTags.add(tag);
            }
        }

        // Não apaga tags existentes se a API externa não retornar tags desta vez.
        if (receivedTags) game.setTags(gameTags);
        gameRepository.save(game);

        // Steam nem sempre publica requisitos. Nesses casos, não criamos registros vazios.
        JsonNode pcRequirements = info.path("pc_requirements");
        if (pcRequirements.isObject()) {
            String minimumHtml = pcRequirements.path("minimum").asText("");
            String recommendedHtml = pcRequirements.path("recommended").asText("");
            GameRequirementsDTO parsed = requirementsParser.parse(minimumHtml, recommendedHtml);
            saveRequirement(game, RequirementType.MINIMUM, parsed.minimum());
            saveRequirement(game, RequirementType.RECOMMENDED, parsed.recommended());
        }
        return true;
    }

    public GameRequirementsDTO previewRequirements(int appId) throws Exception {
        JsonNode info = steamClient.getGameInfo(appId);
        waitBetweenRequests();

        if (info == null || info.isMissingNode() || info.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não foi possível encontrar os dados do jogo " + appId
            );
        }

        JsonNode pcRequirements = info.path("pc_requirements");

        if (!pcRequirements.isObject()) {
            throw new IllegalArgumentException(
                    "O jogo não possui requisitos de PC disponíveis na Steam."
            );
        }

        String minimumHtml = pcRequirements.path("minimum").asText("");
        String recommendedHtml = pcRequirements.path("recommended").asText("");

        System.out.println("========== STEAM REQUIREMENTS ==========");
        System.out.println("Minimum HTML:");
        System.out.println(minimumHtml);
        System.out.println("Recommended HTML:");
        System.out.println(recommendedHtml);
        System.out.println("Minimum length: " + minimumHtml.length());
        System.out.println("Recommended length: " + recommendedHtml.length());
        System.out.println("========================================");

        return requirementsParser.parse(minimumHtml, recommendedHtml);
    }

    public boolean refreshRequirementsForGame(int appId) throws Exception {

        Game game = gameRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Jogo não encontrado no catálogo: " + appId
                ));

        JsonNode info = steamClient.getGameInfo(appId);
        waitBetweenRequests();

        if (info == null || info.isMissingNode() || info.isEmpty()) {
            return false;
        }

        JsonNode pcRequirements = info.path("pc_requirements");

        if (!pcRequirements.isObject()) {
            return false;
        }

        String minimumHtml = pcRequirements.path("minimum").asText("");
        String recommendedHtml = pcRequirements.path("recommended").asText("");

        GameRequirementsDTO parsed = requirementsParser.parse(
                minimumHtml,
                recommendedHtml
        );

        boolean hasMinimumData = hasRequirementData(parsed.minimum());
        boolean hasRecommendedData = hasRequirementData(parsed.recommended());

        if (!hasMinimumData && !hasRecommendedData) {
            return false;
        }

        if (hasMinimumData) {
            saveRequirement(game, RequirementType.MINIMUM, parsed.minimum());
        }

        if (hasRecommendedData) {
            saveRequirement(game, RequirementType.RECOMMENDED, parsed.recommended());
        }

        System.out.println("Requisitos atualizados: " + game.getName()
                + " (AppID: " + appId + ")");

        return true;
    }

    private boolean hasRequirementData(RequirementsSetDTO dto) {
        return dto != null && (
                !dto.cpuOptions().isEmpty()
                        || !dto.gpuOptions().isEmpty()
                        || dto.ramGb() != null
                        || dto.storageGb() != null
        );
    }

    public String refreshRequirementsBatch(int pageNumber, int batchSize) {

        Page<Game> gamesPage = gameRepository.findAll(
                PageRequest.of(
                        pageNumber,
                        batchSize,
                        Sort.by(Sort.Direction.ASC, "appId")
                )
        );

        int updated = 0;
        int skipped = 0;
        int errors = 0;

        for (Game game : gamesPage.getContent()) {
            try {
                boolean success = refreshRequirementsForGame(game.getAppId());

                if (success) {
                    updated++;
                } else {
                    skipped++;
                }

            } catch (Exception ex) {
                errors++;

                System.err.println(
                        "Erro ao atualizar requisitos do jogo "
                                + game.getAppId() + ": " + ex.getMessage()
                );
            }
        }

        return String.format(
                "Lote finalizado! Página: %d | Jogos encontrados: %d | "
                        + "Atualizados: %d | Sem requisitos: %d | Erros: %d | "
                        + "Total de páginas: %d",
                pageNumber,
                gamesPage.getNumberOfElements(),
                updated,
                skipped,
                errors,
                gamesPage.getTotalPages()
        );
    }

    private void saveRequirement(Game game, RequirementType type, RequirementsSetDTO dto) throws Exception {
        if (dto == null) return;
        boolean hasData = !dto.cpuOptions().isEmpty() || !dto.gpuOptions().isEmpty()
                || dto.ramGb() != null || dto.storageGb() != null;
        if (!hasData) return;
        GameRequirement entity = requirementRepository.findByGameAppIdAndType(game.getAppId(), type)
                .orElseGet(GameRequirement::new);
        entity.setGame(game);
        entity.setType(type);
        entity.setCpuOptionsJson(objectMapper.writeValueAsString(dto.cpuOptions()));
        entity.setGpuOptionsJson(objectMapper.writeValueAsString(dto.gpuOptions()));
        entity.setRamGb(dto.ramGb());
        entity.setStorageGb(dto.storageGb());
        requirementRepository.save(entity);
    }

    private Integer parseReleaseYear(String date) {
        if (date == null || date.isBlank()) return null;
        Matcher matcher = Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(date);
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }

    private String nullableText(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void waitBetweenRequests() throws InterruptedException {
        Thread.sleep(1200);
    }

}