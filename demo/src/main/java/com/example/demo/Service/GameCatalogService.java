package com.example.demo.Service;

import com.example.demo.Entity.Game;
import com.example.demo.Repository.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameCatalogService {

    private final GameRepository gameRepository;

    public GameCatalogService(
            GameRepository gameRepository
    ) {
        this.gameRepository = gameRepository;
    }

    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }
}