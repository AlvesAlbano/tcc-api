package com.example.demo.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Service
public class TagFilterService {

    private final Set<String> ignoredTags = new HashSet<>();

    @PostConstruct
    public void loadIgnoredTags() {
        try {
            ClassPathResource resource = new ClassPathResource("ignored-tags.txt");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream())
            );

            reader.lines()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(line -> !line.isBlank())
                    .forEach(ignoredTags::add);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar ignored-tags.txt!", e);
        }
    }

    public boolean shouldIgnore(String tag) {
        return ignoredTags.contains(tag.trim().toLowerCase());
    }

}
