package com.example.demo.Util;

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
        System.out.printf("Antes da formatação (CPU): %s\n", cpuName);

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

        System.out.printf("Depois da formatação (CPU): %s\n", cpuName);
        return cpuName;
    }

    public static String formatGpuName(String gpuName) {
        System.out.printf("Antes da formatação (GPU): %s\n", gpuName);

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
        System.out.printf("Depois da formatação (GPU): %s\n", gpuName);

        return gpuName;
    }
}