package com.example.demo.Controller;

import com.example.demo.Model.ComponentJsonDTO;
import com.example.demo.Service.HardwareComparationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/hardware-comparation")
@Tag(name = "Hardware Comparation")
public class HardwareComparationController {

    private final HardwareComparationService hardwareComparationService;

    public HardwareComparationController(HardwareComparationService hardwareComparationService) {
        this.hardwareComparationService = hardwareComparationService;
    }

    @GetMapping("/compare-cpu")
    @Operation(
            summary = "Compara dois processadores",
            description = "Compara dois modelos de CPU e retorna qual deles possui melhor desempenho.  (Ex. cpu1 = AMD Ryzen 3 210, cpu2 = AMD Ryzen 3 3100)"
    )
    public boolean compareCPU(
            @RequestParam("cpuUser") String cpuUser,
            @RequestParam("cpuGame") String cpuGame
    ) throws IOException, InterruptedException {
        return hardwareComparationService.compareCPU(cpuUser, cpuGame);
    }

    @GetMapping("/compare-gpu")
    @Operation(
            summary = "Compara duas placas de vídeo",
            description = "Compara dois modelos de GPU e retorna qual deles possui melhor desempenho. (Ex. gpu1 = AMD Radeon 820M, gpu2 = Nvidia GeForce MX230)"
    )
    public boolean compareGPU(
            @RequestParam("gpuUser") String gpuUser,
            @RequestParam("gpuGame") String gpuGame
    ) throws IOException, InterruptedException {
        return hardwareComparationService.compareGPU(gpuUser, gpuGame);
    }

    @GetMapping("/available-cpus")
    @Operation(
            summary = "Se utilizar parâmetro cpuName, vai buscar as CPUs através dele, caso contrário, vai listar as 500 CPUs."
    )
    public List<ComponentJsonDTO> getCPUs(
            @RequestParam(value = "cpuName", required = false)
            String cpuName
    ) throws IOException, InterruptedException {
        if (cpuName != null && !cpuName.isBlank()) {
            return hardwareComparationService.dynamicSearchCpu(cpuName);
        }

        return hardwareComparationService.getListCPU();
    }

    @GetMapping("/available-gpus")
    @Operation(
            summary = "Se utilizar parâmetro gpuName, vai buscar as GPUs através dele, caso contrário, vai listar as 500 GPUs."
    )
    public List<ComponentJsonDTO> getGPUs(
            @RequestParam(value = "gpuName", required = false)
            String gpuName
    ) throws IOException, InterruptedException {
        if (gpuName != null && !gpuName.isBlank()) {
            return hardwareComparationService.dynamicSearchGpu(gpuName);
        }

        return hardwareComparationService.getListGPU();
    }
}