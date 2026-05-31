package com.example.demo.Model;

public record UserProfileHeaderDTO(
        String name,
        String avatar,
        Integer totalGames,
        Integer totalFriends,
        Integer totalWishlist
) {
}