
package com.example.demo.Service;

import com.example.demo.Model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SystemRequirementsCheckerService {

    private static final Logger logger = LoggerFactory.getLogger(SystemRequirementsCheckerService.class);
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

    public RequirementsResultsDTO compareRequirements(
            GameRequirementsDTO requirements,
            UserPcInfoDTO userPcInfoDTO
    ) throws InterruptedException {

        if (userPcInfoDTO == null) {
            throw new IllegalArgumentException(
                    "As informações do hardware do usuário são obrigatórias."
            );
        }

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

        Boolean ramMinimum = compareRAM(minimum, userPcInfoDTO);
        Boolean ramRecommended = compareRAM(recommended, userPcInfoDTO);

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
    ) throws InterruptedException {

        Map<String, ComponentRequirementResultDTO> results = new LinkedHashMap<>();

        if (requirements == null
                || requirements.cpuOptions() == null
                || isBlank(userPcInfoDTO.cpu())) {
            return results;
        }

        ComponentDTO userCPU = new ComponentDTO(userPcInfoDTO.cpu(), "user");

        for (String option : requirements.cpuOptions()) {
            if (isBlank(option)) {
                continue;
            }

            try {
                ComponentDTO gameCPU = new ComponentDTO(option, "game");
                String result = hardwareComparationService.compareCPU(userCPU, gameCPU);

                results.put(option, createResult(result));

            } catch (IOException exception) {
                logger.warn(
                        "Não foi possível comparar o CPU '{}' com '{}': {}",
                        userPcInfoDTO.cpu(),
                        option,
                        exception.getMessage()
                );

                results.put(option, unavailableResult());
            }
        }

        return results;
    }

    private Map<String, ComponentRequirementResultDTO> compareGPUOptions(
            RequirementsSetDTO requirements,
            UserPcInfoDTO userPcInfoDTO
    ) throws InterruptedException {

        Map<String, ComponentRequirementResultDTO> results = new LinkedHashMap<>();

        if (requirements == null
                || requirements.gpuOptions() == null
                || isBlank(userPcInfoDTO.gpu())) {
            return results;
        }

        ComponentDTO userGPU = new ComponentDTO(userPcInfoDTO.gpu(), "user");

        for (String option : requirements.gpuOptions()) {
            if (isBlank(option)) {
                continue;
            }

            try {
                ComponentDTO gameGPU = new ComponentDTO(option, "game");
                String result = hardwareComparationService.compareGPU(userGPU, gameGPU);

                results.put(option, createResult(result));

            } catch (IOException exception) {
                logger.warn(
                        "Não foi possível comparar a GPU '{}' com '{}': {}",
                        userPcInfoDTO.gpu(),
                        option,
                        exception.getMessage()
                );

                results.put(option, unavailableResult());
            }
        }

        return results;
    }

    private Boolean compareRAM(
            RequirementsSetDTO requirements,
            UserPcInfoDTO userPcInfoDTO
    ) {

        if (requirements == null || requirements.ramGb() == null) {
            return null;
        }

        return userPcInfoDTO.ram() >= requirements.ramGb();
    }

    private ComponentRequirementResultDTO createResult(String result) {

        if (result == null || "N/A".equalsIgnoreCase(result)) {
            return unavailableResult();
        }

        return new ComponentRequirementResultDTO(
                Boolean.parseBoolean(result),
                true
        );
    }

    private ComponentRequirementResultDTO unavailableResult() {
        return new ComponentRequirementResultDTO(false, false);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
