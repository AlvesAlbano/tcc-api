package com.example.demo.Service;

import com.example.demo.Entity.GameRequirement;
import com.example.demo.Entity.RequirementType;
import com.example.demo.Model.GameRequirementsDTO;
import com.example.demo.Model.RequirementsSetDTO;
import com.example.demo.Repository.GameRequirementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameRequirementsService {

    private final GameRequirementRepository requirementRepository;
    private final ObjectMapper objectMapper;

    public GameRequirementsService(
            GameRequirementRepository requirementRepository,
            ObjectMapper objectMapper
    ) {
        this.requirementRepository = requirementRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public GameRequirementsDTO getRequirements(Integer appId)
            throws JsonProcessingException {

        RequirementsSetDTO minimum = null;
        RequirementsSetDTO recommended = null;

        List<GameRequirement> requirements =
                requirementRepository.findAllByGameAppId(appId);

        for (GameRequirement item : requirements) {

            RequirementsSetDTO dto = new RequirementsSetDTO(
                    readList(item.getCpuOptionsJson()),
                    readList(item.getGpuOptionsJson()),
                    item.getRamGb(),
                    item.getStorageGb()
            );

            if (item.getType() == RequirementType.MINIMUM) {
                minimum = dto;
            } else if (item.getType() == RequirementType.RECOMMENDED) {
                recommended = dto;
            }
        }

        if (minimum == null && recommended == null) {
            return null;
        }

        return new GameRequirementsDTO(minimum, recommended);
    }

    private List<String> readList(String json)
            throws JsonProcessingException {

        if (json == null || json.isBlank()) {
            return List.of();
        }

        return objectMapper.readValue(
                json,
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class)
        );
    }
}