package com.example.demo.Service;

import com.example.demo.Model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

@Service
public class SystemRequirementsCheckerService {

    private final GameService gameService;
    private final HardwareComparationService hardwareComparationService;

    public SystemRequirementsCheckerService(GameService gameService, HardwareComparationService hardwareComparationService) {
        this.gameService = gameService;
        this.hardwareComparationService = hardwareComparationService;
    }

    public RequirementsResultsDTO requirementsComparation(int appId, UserPcInfoDTO userPcInfoDTO) throws IOException, InterruptedException {
        GameRequirementsDTO gameRequirementsDTO = gameService.getGameRequirements(appId);

        return compareRequirements(gameRequirementsDTO, userPcInfoDTO);
    }

    private RequirementsResultsDTO compareRequirements(GameRequirementsDTO gameRequirementsDTO, UserPcInfoDTO userPcInfoDTO) throws IOException, InterruptedException {

        RequirementsSetDTO minimumRequirements = gameRequirementsDTO.minimum();
        RequirementsSetDTO recommendedRequirements = gameRequirementsDTO.recommended();

        System.out.println(recommendedRequirements);

        // Inicializando os HashMaps
        HashMap<String, String> cpuMinimumRequirements = new HashMap<>();
        HashMap<String, String> cpuRecommendedRequirements = new HashMap<>();
        HashMap<String, String> gpuMinimumRequirements = new HashMap<>();
        HashMap<String, String> gpuRecommendedRequirements = new HashMap<>();
        final boolean ramRecommended = (userPcInfoDTO.ram() >= minimumRequirements.ramGb());
        final boolean ramMinimum = (userPcInfoDTO.ram() >= recommendedRequirements.ramGb());

        // Criação dos componentes do usuário (já com os dados do DTO)
        ComponentDTO gpuUser = new ComponentDTO(userPcInfoDTO.gpu(), "user");
        ComponentDTO cpuUser = new ComponentDTO(userPcInfoDTO.cpu(), "user");

        // ==========================================
        // 1. COMPARAÇÃO DE GPU (Mínimo e Recomendado)
        // ==========================================

        // Mínimo
        if (minimumRequirements != null && minimumRequirements.gpuOptions() != null) {
            for (String gpuOption : minimumRequirements.gpuOptions()) {
                ComponentDTO gpuGame = new ComponentDTO(gpuOption, "game");
                String result = hardwareComparationService.compareGPU(gpuUser, gpuGame);
                gpuMinimumRequirements.put(gpuOption, result);
            }
        }

        // Recomendado
        if (recommendedRequirements != null && recommendedRequirements.gpuOptions() != null) {
            for (String gpuOption : recommendedRequirements.gpuOptions()) {
                ComponentDTO gpuGame = new ComponentDTO(gpuOption, "game");
                String result = hardwareComparationService.compareGPU(gpuUser, gpuGame);
                gpuRecommendedRequirements.put(gpuOption, result);
            }
        }

        // ==========================================
        // 2. COMPARAÇÃO DE CPU (Mínimo e Recomendado)
        // ==========================================

        // Mínimo
        if (minimumRequirements != null && minimumRequirements.cpuOptions() != null) {
            for (String cpuOption : minimumRequirements.cpuOptions()) {
                ComponentDTO cpuGame = new ComponentDTO(cpuOption, "game");
                // Supondo que você tenha um método parecesido para CPU:
                String result = hardwareComparationService.compareCPU(cpuUser, cpuGame);
                cpuMinimumRequirements.put(cpuOption, result);
            }
        }

        // Recomendado
        if (recommendedRequirements != null && recommendedRequirements.cpuOptions() != null) {
            for (String cpuOption : recommendedRequirements.cpuOptions()) {
                ComponentDTO cpuGame = new ComponentDTO(cpuOption, "game");
                String result = hardwareComparationService.compareCPU(cpuUser, cpuGame);
                cpuRecommendedRequirements.put(cpuOption, result);
            }
        }

        return new RequirementsResultsDTO(
                cpuMinimumRequirements,
                cpuRecommendedRequirements,
                gpuMinimumRequirements,
                gpuRecommendedRequirements,
                ramMinimum,
                ramRecommended
        );
    }

}