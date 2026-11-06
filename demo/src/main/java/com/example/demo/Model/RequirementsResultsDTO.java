package com.example.demo.Model;

import java.util.HashMap;
import java.util.Map;

public record RequirementsResultsDTO(
        Map<String, ComponentRequirementResultDTO> cpuMinimumRequirements,
        Map<String, ComponentRequirementResultDTO> cpuRecommendedRequirements,
        Map<String, ComponentRequirementResultDTO> gpuMinimumRequirements,
        Map<String, ComponentRequirementResultDTO> gpuRecommendedRequirements,
        Boolean ramMinimum,
        Boolean ramRecommended
) {

}
