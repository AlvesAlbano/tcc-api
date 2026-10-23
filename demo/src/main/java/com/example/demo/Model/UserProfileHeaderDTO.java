package com.example.demo.Model;

import java.util.List;

public record UserProfileHeaderDTO(
        String name,
        String avatar,
        Integer totalGames,
        Integer totalFriends,
        Integer totalWishlist,
        List<String> favoriteGenres
) {
}