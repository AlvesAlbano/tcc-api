package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "game_requirements", uniqueConstraints =
@UniqueConstraint(name = "uk_game_requirement_type", columnNames = {"game_id", "requirement_type"}))
@Getter
@Setter
@NoArgsConstructor
public class GameRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, length = 20)
    private RequirementType type;

    // As listas são armazenadas em JSON para preservar alternativas de CPU/GPU.
    @Column(name = "cpu_options_json", columnDefinition = "TEXT")
    private String cpuOptionsJson;

    @Column(name = "gpu_options_json", columnDefinition = "TEXT")
    private String gpuOptionsJson;

    @Column(name = "ram_gb")
    private Integer ramGb;

    @Column(name = "storage_gb")
    private Integer storageGb;
}
