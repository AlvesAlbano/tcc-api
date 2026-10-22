package com.example.demo.Model;

import java.util.List;

public record RequirementsSetDTO(
        List<String> cpuOptions,
        List<String> gpuOptions,
        Integer ramGb,
        Integer storageGb
) {
}
