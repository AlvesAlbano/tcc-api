package com.example.demo.Service;


import com.example.demo.CompareCPU;
import com.example.demo.CompareGPU;
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

    public boolean compareCPU(String cpuUser,String cpuGame) throws IOException, InterruptedException {

        cpuUser = formatCpuString(cpuUser);
        cpuGame = formatCpuString(cpuGame);

        return compareCPU.comparar(cpuUser, cpuGame);
    }

    public boolean compareGPU(String gpuUser,String gpuGame) throws IOException, InterruptedException {
        gpuUser = formatGpuString(gpuUser);
        gpuGame = formatGpuString(gpuGame);

        return compareGPU.comparar(gpuUser, gpuGame);
    }

    public List<ComponentJsonDTO> getListCPU() throws IOException, InterruptedException {
        return scrapperCPU.cpusDisponiveis();
    }

    public List<ComponentJsonDTO> getListGPU() throws IOException, InterruptedException {
        return scrapperGPU.gpusDisponiveis();
    }

    private String formatGpuString(String gpuName){
        System.out.printf("Antes da formatação: %s\n",gpuName);

        final String[] keyWords = {
                "Nvidia",
                "AMD"
        };

        for(String keyWord: keyWords){
            if(gpuName.contains(keyWord)){
                gpuName = gpuName
                        .replace(keyWord,"")
                        .toLowerCase()
                        .trim()
                        .replace(" ","-");
            }
        }

        System.out.printf("Depois da formatação: %s\n",gpuName);
        return gpuName;
    }

    private String formatCpuString(String CpuName){
        System.out.printf("Antes da formatação: %s\n",CpuName);

        CpuName = CpuName
                .replace(" ","-")
                .toLowerCase()
                .trim();

        System.out.printf("Depois da formatação: %s\n",CpuName);
        return CpuName;
    }
}
