package com.example.demo.Repository;

import com.example.demo.Entity.GameRequirement;
import com.example.demo.Entity.RequirementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRequirementRepository extends JpaRepository<GameRequirement, Long> {
    List<GameRequirement> findAllByGameAppId(Integer appId);

    Optional<GameRequirement> findByGameAppIdAndType(Integer appId, RequirementType type);
}
