package com.weatherx.backend.controller;

import com.weatherx.backend.model.FavoriteCity;
import com.weatherx.backend.repository.FavoriteCityRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:5173")
public class FavoriteCityController {

    private final FavoriteCityRepository repository;

    public FavoriteCityController(
            FavoriteCityRepository repository
    ) {
        this.repository = repository;
    }

    // =========================
    // GET FAVORITES
    // =========================

    @GetMapping
    public List<FavoriteCity> getFavorites(
            @RequestParam Long userId
    ) {
        return repository.findByUserId(userId);
    }

    // =========================
    // ADD FAVORITE
    // =========================

    @PostMapping
    public FavoriteCity addFavorite(
            @RequestParam Long userId,
            @RequestParam String city
    ) {

        String cleanCity = city.trim();

        if (cleanCity.isEmpty()) {
            throw new RuntimeException(
                    "City name cannot be empty."
            );
        }

        if (repository
                .findByUserIdAndCity(userId, cleanCity)
                .isPresent()) {

            throw new RuntimeException(
                    "City already added to favorites."
            );
        }

        FavoriteCity favorite =
                new FavoriteCity(userId, cleanCity);

        return repository.save(favorite);
    }

    // =========================
    // DELETE FAVORITE
    // =========================

    @DeleteMapping
    @Transactional
    public String removeFavorite(
            @RequestParam Long userId,
            @RequestParam String city
    ) {

        String cleanCity = city.trim();

        int deleted =
                repository.deleteFavorite(
                        userId,
                        cleanCity
                );

        if (deleted == 0) {
            return "Favorite city not found.";
        }

        return "Favorite city removed.";
    }
}