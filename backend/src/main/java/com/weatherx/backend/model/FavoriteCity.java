package com.weatherx.backend.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "favourites",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_user_city",
            columnNames = {"user_id", "city"}
        )
    }
)
public class FavoriteCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String city;

    public FavoriteCity() {
    }

    public FavoriteCity(Long userId, String city) {
        this.userId = userId;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCity() {
        return city;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCity(String city) {
        this.city = city;
    }
}