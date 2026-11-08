package com.example.demo.Controller;

import com.example.demo.Model.RequirementsResultsDTO;
import com.example.demo.Model.UserPcInfoDTO;
import com.example.demo.Service.SystemRequirementsCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("requirements-checker")
@Tag(name = "Requirements Checker")
public class SystemRequirementsCheckerController {

    private final SystemRequirementsCheckerService systemRequirementsCheckerService;

    public SystemRequirementsCheckerController(SystemRequirementsCheckerService systemRequirementsCheckerService) {
        this.systemRequirementsCheckerService = systemRequirementsCheckerService;
    }

    @PostMapping("check-result")
    @Operation(
            summary = "Retorna um json contendo os resultados da comparação entre o hardware do usuario e jogo, False = Hardware do jogo é melhor, True = Hardware do usuario é melhor, N/A = Hardware nã encontrado no nanoreview",
            description = "(AppId jogo: 3259780, Json teste = {\n" +
                    "    \"cpu\": \"amd-ryzen-3-3100\",\n" +
                    "    \"gpu\": \"geforce-mx230\",\n" +
                    "    \"ram\": 16\n" +
                    "})"
    )
    public RequirementsResultsDTO getRequirementsCheckerResults(
            @RequestParam(name = "appId") int appId,
            @RequestBody UserPcInfoDTO userPcInfoDTO) throws IOException, InterruptedException {
        return systemRequirementsCheckerService.requirementsComparation(appId, userPcInfoDTO);
    }
}
