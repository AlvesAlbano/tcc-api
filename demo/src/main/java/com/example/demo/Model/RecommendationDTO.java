package com.example.demo.Model;

import java.util.List;

public record RecommendationDTO(
        Integer appId,
        String name,
        String shortDescription,
        String imageUrl,
        Integer releaseYear,
        List<String> tags,
        GameRequirementsDTO requirements,
        Double score
) {
}
