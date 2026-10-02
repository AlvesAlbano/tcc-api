package com.example.demo.Controller;

import com.example.demo.Model.ComponentJsonDTO;
import com.example.demo.Service.HardwareComparationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/hardware-comparation")
@Tag(name = "Hardware Comparation")
public class HardwareComparationController {

    @Autowired
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

    @GetMapping("/avaliable-cpus")
    @Operation(
            summary = "Lista os processadores disponíveis",
            description = "Retorna a lista de processadores disponíveis para comparação."
    )
    public List<ComponentJsonDTO> getListCPU()
            throws IOException, InterruptedException {
        return hardwareComparationService.getListCPU();
    }

    @GetMapping("/avaliable-gpus")
    @Operation(
            summary = "Lista as placas de vídeo disponíveis",
            description = "Retorna a lista de placas de vídeo disponíveis para comparação."
    )
    public List<ComponentJsonDTO> getListGPU()
            throws IOException, InterruptedException {
        return hardwareComparationService.getListGPU();
    }
}