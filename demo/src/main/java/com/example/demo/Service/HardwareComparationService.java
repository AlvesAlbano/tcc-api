package com.example.demo.Service;


import com.example.demo.CompareCPU;
import com.example.demo.CompareGPU;
import com.example.demo.Model.ComponentDTO;
import com.example.demo.Model.ComponentJsonDTO;
import com.example.demo.ScrapperCPU;
import com.example.demo.ScrapperGPU;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class HardwareComparationService {

    private final ScrapperCPU scrapperCPU;

    private final ScrapperGPU scrapperGPU;

    private final CompareGPU compareGPU;

    private final CompareCPU compareCPU;

    public HardwareComparationService(ScrapperCPU scrapperCPU, ScrapperGPU scrapperGPU, CompareGPU compareGPU, CompareCPU compareCPU) {
        this.scrapperCPU = scrapperCPU;
        this.scrapperGPU = scrapperGPU;
        this.compareGPU = compareGPU;
        this.compareCPU = compareCPU;
    }

    public String compareCPU(ComponentDTO cpuUser, ComponentDTO cpuGame) throws IOException, InterruptedException {
        String cpuGameName = cpuGame.name();
        String formatedcpuGameName = cleanHardwareString(cpuGame.name());

        if (cpuExistInScrapper(cpuGameName)) {
//            return compareCPU.comparar(cpuUser, cpuGame);
            return String.valueOf(compareCPU.comparar(cpuUser, cpuGame));
        } else if (cpuExistInScrapper(formatedcpuGameName)) {

            ComponentDTO cpuGameFormated = new ComponentDTO(formatedcpuGameName, "game");

            return String.valueOf(compareCPU.comparar(cpuUser, cpuGameFormated));
        }

        return "N/A";
    }

    public String compareGPU(ComponentDTO gpuUser, ComponentDTO gpuGame) throws IOException, InterruptedException {
        String gpuGameName = gpuGame.name();
        String formatedGpuGameName = cleanHardwareString(gpuGame.name());

        if (gpuExistInScrapper(gpuGameName)) {
//            return compareGPU.comparar(gpuUser, gpuGame);

            return String.valueOf(compareGPU.comparar(gpuUser, gpuGame));
        } else if (gpuExistInScrapper(formatedGpuGameName)) {

            ComponentDTO gpuGameFormated = new ComponentDTO(formatedGpuGameName, "game");

//            return compareGPU.comparar(gpuUser, gpuGameFormated);
            return String.valueOf(compareGPU.comparar(gpuUser, gpuGameFormated));
        }

        return "N/A";
    }

    public List<ComponentJsonDTO> getListCPU() throws IOException, InterruptedException {
        return scrapperCPU.cpusDisponiveis();
    }

    public List<ComponentJsonDTO> getListGPU() throws IOException, InterruptedException {
        return scrapperGPU.gpusDisponiveis();
    }

    public List<ComponentJsonDTO> dynamicSearchGpu(String gpuName) throws IOException, InterruptedException {
        return scrapperGPU.dynamicSearch(gpuName);
    }

    public List<ComponentJsonDTO> dynamicSearchCpu(String cpuName) throws IOException, InterruptedException {
        return scrapperCPU.dynamicSearch(cpuName);
    }

    private boolean gpuExistInScrapper(String gpuName) throws IOException, InterruptedException {
        List<ComponentJsonDTO> gpuList = scrapperGPU.dynamicSearch(gpuName);

        for (ComponentJsonDTO gpu : gpuList) {
            if (gpu.slug().equalsIgnoreCase(gpuName)) {
                return true;
            }
        }
        return false;
    }

    private boolean cpuExistInScrapper(String cpuName) throws IOException, InterruptedException {
        List<ComponentJsonDTO> cpuList = scrapperCPU.dynamicSearch(cpuName);

        for (ComponentJsonDTO cpu : cpuList) {
            if (cpu.slug().equalsIgnoreCase(cpuName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove trechos comuns de especificações de hardware da Steam (VRAM, or more, clock, GB/MB, etc.)
     */
    private String cleanHardwareString(String value) {
        return value
                // 1. Remove qualquer coisa que esteja entre parênteses (ex: "(VRAM 2GB)", "(DirectX 11)", "(2 GB)")
                .replaceAll("(?i)\\([^)]*\\)", "")

                // 2. Remove termos como "or more", "or higher", "ou superior" e tudo o que vier depois no final da string
                .replaceAll("(?i)\\b(or\\s+(?:more|higher|better|compatible)|ou\\s+(?:superior|mais))\b.*$", "")

                // 3. Remove frequências de clock (ex: "3.2 GHz", "3GHz", "2400MHz")
                .replaceAll("(?i)\\b\\d+(?:\\.\\d+)?\\s*(?:ghz|mhz)\\b", "")

                // 4. Remove menções a VRAM ou Memória soltas (ex: "VRAM 2GB", "memory: 4gb")
                .replaceAll("(?i)\\b(?:vram|memory)\\s*[:\\-]?\\s*\\d*\\s*(?:gb|mb)?\\b", "")

                // 5. NOVO/AJUSTADO: Remove especificações de memória/VRAM coladas ou não (ex: "6GB", "8 GB", "2048MB", "4G")
                // O uso de \d+\s*(?:g(?:b)?|mb)\b garante que pegue "6GB", "8GB", "2G", etc.
                .replaceAll("(?i)\\b\\d+\\s*(?:g(?:b)?|mb)\\b", "")

                .trim();
    }
}
