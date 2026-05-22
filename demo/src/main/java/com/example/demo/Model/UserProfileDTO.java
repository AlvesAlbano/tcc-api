package com.example.demo.Model;

import java.util.Map;
import java.util.Set;

public record UserProfileDTO(
        Set<Integer> ownedGames,
        Map<String, Double> tagWeights
) {
}
