package com.example.demo.Util;

import java.util.HashMap;
import java.util.Map;

public final class FormatationComponentUtil {

    private static final String[] gpuKeyWords = {
            "Nvidia",
            "AMD",
            "™",
            "℠",
            "®",
            "©",
            "@",
            "VRAM",
    };

    private static final String[] cpuKeyWords = {
            "™",
            "℠",
            "®",
            "©",
            "@",
    };

    public FormatationComponentUtil() {
    }

    public static String formatCpuName(String cpuName) {
//        System.out.printf("Antes da formatação (CPU): %s\n", cpuName);

        for (String keyWord : cpuKeyWords) {
            cpuName = cpuName.replaceAll(
                    "(?i)" + java.util.regex.Pattern.quote(keyWord),
                    ""
            );
        }

        cpuName = cpuName
                .trim()
                .replace(" ", "-")
                .toLowerCase();

//        System.out.printf("Depois da formatação (CPU): %s\n", cpuName);
        return cpuName;
    }

    public static String formatGpuName(String gpuName) {
//        System.out.printf("Antes da formatação (GPU): %s\n", gpuName);

        for (String keyWord : gpuKeyWords) {
            gpuName = gpuName.replaceAll(
                    "(?i)" + java.util.regex.Pattern.quote(keyWord),
                    ""
            );
        }

        gpuName = gpuName
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-");
//        System.out.printf("Depois da formatação (GPU): %s\n", gpuName);

        return gpuName;
    }

    public static String cpuAddMissingInfo(String cpu) {
        HashMap<String, String> nameRules = new HashMap<>(Map.of(
                "intel", "core",
                "amd", "ryzen"
        ));

        for (Map.Entry<String, String> entry : nameRules.entrySet()) {
            String brand = entry.getKey();
            String info = entry.getValue();

            if (cpu.startsWith(brand) && !cpu.contains(info)) {
                return cpu.replaceFirst("(?i)" + brand, brand + "-" + info);
            }
        }

        return cpu;
    }

    public static String gpuAddMissingInfo(String gpu) {
        HashMap<String, String> nameRules = new HashMap<>(Map.of(
                "nvidia", "geforce",
                "amd", "radeon"
        ));

        for (Map.Entry<String, String> entry : nameRules.entrySet()) {
            String brand = entry.getKey();
            String info = entry.getValue();

            if (gpu.startsWith(brand) && !gpu.contains(info)) {
                return gpu.replaceFirst("(?i)" + brand, brand + "-" + info);
            }
        }
        return gpuAddMissingInfoBefore(gpu);
    }

    private static String gpuAddMissingInfoBefore(String gpu) {
        HashMap<String, String> nameRules = new HashMap<>(Map.of(
                "gtx","geforce",
                "rx","radeon"
        ));

        for (Map.Entry<String, String> entry : nameRules.entrySet()) {
            String model = entry.getKey();
            String info = entry.getValue();

            if (gpu.toLowerCase().startsWith(model) && !gpu.toLowerCase().startsWith(info)) {
                gpu = info + "-" + gpu;
            }
        }

        return gpu;
    }
}