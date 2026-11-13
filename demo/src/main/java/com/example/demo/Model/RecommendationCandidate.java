package com.example.demo.Model;

import com.example.demo.Entity.Game;

import java.util.List;

public record RecommendationCandidate(
        Game game,
        List<String> tags,
        double score
) {}
