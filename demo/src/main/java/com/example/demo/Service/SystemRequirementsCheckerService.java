
package com.example.demo.Service;

import com.example.demo.Model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SystemRequirementsCheckerService {

    private final GameRequirementsService gameRequirementsService;
    private final HardwareComparationService hardwareComparationService;

    public SystemRequirementsCheckerService(
            GameRequirementsService gameRequirementsService,
            HardwareComparationService hardwareComparationService
    ) {
        this.gameRequirementsService = gameRequirementsService;
        this.hardwareComparationService = hardwareComparationService;
    }

    public RequirementsResultsDTO requirementsComparation(
            int appId,
            UserPcInfoDTO userPcInfoDTO
    ) throws IOException, InterruptedException {

        GameRequirementsDTO requirements = gameRequirementsService.getRequirements(appId);
        return compareRequirements(requirements, userPcInfoDTO);
    }

    // Compara os requisitos já carregados do banco.
    public RequirementsResultsDTO compareRequirements(
            GameRequirementsDTO requirements,
            UserPcInfoDTO userPcInfoDTO
    ) throws IOException, InterruptedException {

        RequirementsSetDTO minimum = requirements != null
                ? requirements.minimum() : null;

        RequirementsSetDTO recommended = requirements != null
                ? requirements.recommended() : null;

        Map<String, ComponentRequirementResultDTO> cpuMinimum =
                compareCPUOptions(minimum, userPcInfoDTO);

        Map<String, ComponentRequirementResultDTO> cpuRecommended =
                compareCPUOptions(recommended, userPcInfoDTO);

        Map<String, ComponentRequirementResultDTO> gpuMinimum =
                compareGPUOptions(minimum, userPcInfoDTO);

        Map<String, ComponentRequirementResultDTO> gpuRecommended =
                compareGPUOptions(recommended, userPcInfoDTO);

        Boolean ramMinimum = minimum != null && minimum.ramGb() != null
                ? userPcInfoDTO.ram() >= minimum.ramGb()
                : null;

        Boolean ramRecommended = recommended != null
                && recommended.ramGb() != null
                ? userPcInfoDTO.ram() >= recommended.ramGb()
                : null;

        return new RequirementsResultsDTO(
                cpuMinimum,
                cpuRecommended,
                gpuMinimum,
                gpuRecommended,
                ramMinimum,
                ramRecommended
        );
    }

    private Map<String, ComponentRequirementResultDTO> compareCPUOptions(
            RequirementsSetDTO requirements,
            UserPcInfoDTO userPcInfoDTO
    ) throws IOException, InterruptedException {

        Map<String, ComponentRequirementResultDTO> results = new HashMap<>();

        if (requirements == null || requirements.cpuOptions() == null) {
            return results;
        }

        ComponentDTO userCPU = new ComponentDTO(userPcInfoDTO.cpu(), "user");

        for (String option : requirements.cpuOptions()) {
            if (option == null || option.isBlank()) {
                continue;
            }

            ComponentDTO gameCPU = new ComponentDTO(option, "game");
            String result = hardwareComparationService.compareCPU(userCPU, gameCPU);
            results.put(option, createResult(result));
        }

        return results;
    }

    private Map<String, ComponentRequirementResultDTO> compareGPUOptions(
            RequirementsSetDTO requirements,
            UserPcInfoDTO userPcInfoDTO
    ) throws IOException, InterruptedException {

        Map<String, ComponentRequirementResultDTO> results = new HashMap<>();

        if (requirements == null || requirements.gpuOptions() == null) {
            return results;
        }

        ComponentDTO userGPU = new ComponentDTO(userPcInfoDTO.gpu(), "user");

        for (String option : requirements.gpuOptions()) {
            if (option == null || option.isBlank()) {
                continue;
            }

            ComponentDTO gameGPU = new ComponentDTO(option, "game");
            String result = hardwareComparationService.compareGPU(userGPU, gameGPU);
            results.put(option, createResult(result));
        }

        return results;
    }

    private ComponentRequirementResultDTO createResult(String result) {
        if (result == null || "N/A".equalsIgnoreCase(result)) {
            return new ComponentRequirementResultDTO(false, false);
        }

        return new ComponentRequirementResultDTO(Boolean.parseBoolean(result), true);
    }

    public boolean meetsMinimumRequirements(
            RequirementsResultsDTO compatibility
    ) {
        if (compatibility == null) {
            return true;
        }

        Boolean cpu = evaluateOptions(compatibility.cpuMinimumRequirements());
        Boolean gpu = evaluateOptions(compatibility.gpuMinimumRequirements());

        // null é pq é desconhecido, portanto nao elimina o jogo
        return !Boolean.FALSE.equals(cpu)
                && !Boolean.FALSE.equals(gpu)
                && !Boolean.FALSE.equals(compatibility.ramMinimum());
    }

    private Boolean evaluateOptions(
            Map<String, ComponentRequirementResultDTO> results
    ) {
        if (results == null || results.isEmpty()) {
            return null;
        }

        boolean hasAvailableComparison = false;

        for (ComponentRequirementResultDTO result : results.values()) {
            if (result == null || !result.available()) {
                continue;
            }

            hasAvailableComparison = true;

            if (result.compatible()) {
                return true;
            }
        }

        return hasAvailableComparison ? false : null;
    }

    // pontuacao hardware dos requisitos recomendados
    public double calculateHardwareScore(
            RequirementsResultsDTO compatibility
    ) {
        if (compatibility == null) {
            return 0.5;
        }

        Boolean cpu = evaluateOptions(compatibility.cpuRecommendedRequirements());
        Boolean gpu = evaluateOptions(compatibility.gpuRecommendedRequirements());
        Boolean ram = compatibility.ramRecommended();

        double total = 0;
        int evaluatedComponents = 0;

        if (cpu != null) {
            total += cpu ? 1.0 : 0.0;
            evaluatedComponents++;
        }

        if (gpu != null) {
            total += gpu ? 1.0 : 0.0;
            evaluatedComponents++;
        }

        if (ram != null) {
            total += ram ? 1.0 : 0.0;
            evaluatedComponents++;
        }

        // Sem dados recomendados: pontuação neutra.
        if (evaluatedComponents == 0) {
            return 0.5;
        }

        return total / evaluatedComponents;
    }
}
