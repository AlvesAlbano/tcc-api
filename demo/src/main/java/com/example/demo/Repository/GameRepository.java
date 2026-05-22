package com.example.demo.Repository;

import com.example.demo.Entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository
        extends JpaRepository<Game, Integer> {
}