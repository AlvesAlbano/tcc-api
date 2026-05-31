package com.example.demo.Model;

public record RecommendationDTO(
        Integer appId,
        String name,
        String shortDescription,
        String imageUrl,
        Double score
) {
}
