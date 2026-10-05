package com.example.demo.Model;

public record GameRequirementsDTO(
        RequirementsSetDTO minimum,
        RequirementsSetDTO recommended
) {
}
