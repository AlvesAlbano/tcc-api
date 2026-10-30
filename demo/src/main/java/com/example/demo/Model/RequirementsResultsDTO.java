package com.example.demo.Model;

import java.util.HashMap;

public record RequirementsResultsDTO(
        HashMap<String, String> cpuMinimumRequirements,
        HashMap<String, String> cpuRecommendedRequirements,
        HashMap<String, String> gpuMinimumRequirements,
        HashMap<String, String> gpuRecommendedRequirements,
        boolean ramMinimum,
        boolean ramRecommended
) {
}
