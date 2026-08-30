package com.weatherx.backend.repository;

import com.weatherx.backend.model.FavoriteCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteCityRepository
        extends JpaRepository<FavoriteCity, Long> {

    List<FavoriteCity> findByUserId(Long userId);

    Optional<FavoriteCity> findByUserIdAndCity(
            Long userId,
            String city
    );

    @Modifying
    @Query("""
        DELETE FROM FavoriteCity f
        WHERE f.userId = :userId
        AND f.city = :city
    """)
    int deleteFavorite(
            @Param("userId") Long userId,
            @Param("city") String city
    );
}