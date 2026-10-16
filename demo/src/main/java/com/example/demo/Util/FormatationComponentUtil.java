package com.example.demo.Util;

public final class FormatationComponentUtil {
    public FormatationComponentUtil() {
    }

    public static String formatCpuName(String cpuName){
        System.out.printf("Antes da formatação (CPU): %s\n",cpuName);

        cpuName = cpuName
                .replace(" ","-")
                .toLowerCase()
                .trim();

        System.out.printf("Depois da formatação (CPU): %s\n",cpuName);
        return cpuName;
    }

    public static String formatGpuName(String gpuName){
        System.out.printf("Antes da formatação (GPU): %s\n",gpuName);

        final String[] keyWords = {
                "Nvidia",
                "AMD",
                "™",
                "℠",
                "®",
                "©"
        };

        for (String keyWord : keyWords) {
            gpuName = gpuName.replaceAll(
                    "(?i)" + java.util.regex.Pattern.quote(keyWord),
                    ""
            );
        }

        gpuName = gpuName
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-");
        System.out.printf("Depois da formatação (GPU): %s\n",gpuName);

        return gpuName;
    }
}
