package com.weatherx.backend.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/api/weather")
    public ResponseEntity<?> getWeather(@RequestParam String city) {

        try {

            if (city == null || city.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error",
                                "City name is required."
                        ));
            }

            String cleanCity = city.trim();

            String encodedCity = URLEncoder.encode(
                    cleanCity,
                    StandardCharsets.UTF_8
            );

            String geoUrl =
                    "https://geocoding-api.open-meteo.com/v1/search"
                    + "?name=" + encodedCity
                    + "&count=1"
                    + "&language=en"
                    + "&format=json";

            System.out.println("Geocoding URL: " + geoUrl);

            String locationResponse =
                    restTemplate.getForObject(
                            geoUrl,
                            String.class
                    );

            System.out.println(
                    "Geocoding response: "
                            + locationResponse
            );

            if (locationResponse == null
                    || locationResponse.isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error",
                                "No response received from weather service."
                        ));
            }

            JsonNode locationJson =
                    objectMapper.readTree(locationResponse);

            JsonNode results =
                    locationJson.get("results");

            if (results == null
                    || !results.isArray()
                    || results.isEmpty()) {

                return ResponseEntity
                        .status(404)
                        .body(Map.of(
                                "error",
                                "City not found: " + cleanCity
                        ));
            }

            JsonNode location = results.get(0);

            if (!location.has("latitude")
                    || !location.has("longitude")) {

                return ResponseEntity
                        .status(404)
                        .body(Map.of(
                                "error",
                                "Location coordinates not found."
                        ));
            }

            double latitude =
                    location.get("latitude").asDouble();

            double longitude =
                    location.get("longitude").asDouble();

            System.out.println(
                    "Location found: "
                            + latitude
                            + ", "
                            + longitude
            );

            String weatherData =
                    getWeatherData(
                            latitude,
                            longitude
                    );

            return ResponseEntity.ok(
                    objectMapper.readTree(weatherData)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "error",
                            "Weather service error.",
                            "message",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unknown error"
                    ));
        }
    }

    @GetMapping("/api/weather/location")
    public ResponseEntity<?> getWeatherByLocation(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {

        try {

            String weatherData =
                    getWeatherData(
                            latitude,
                            longitude
                    );

            return ResponseEntity.ok(
                    objectMapper.readTree(weatherData)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "error",
                            "Unable to fetch weather.",
                            "message",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unknown error"
                    ));
        }
    }

    private String getWeatherData(
            double latitude,
            double longitude
    ) {

        String weatherUrl =
                "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current="
                + "temperature_2m,"
                + "relative_humidity_2m,"
                + "apparent_temperature,"
                + "is_day,"
                + "precipitation,"
                + "weather_code,"
                + "wind_speed_10m,"
                + "surface_pressure"
                + "&hourly="
                + "temperature_2m,"
                + "relative_humidity_2m,"
                + "apparent_temperature,"
                + "precipitation_probability,"
                + "precipitation,"
                + "weather_code,"
                + "wind_speed_10m"
                + "&forecast_days=7"
                + "&timezone=auto";

        System.out.println(
                "Weather URL: " + weatherUrl
        );

        String response =
                restTemplate.getForObject(
                        weatherUrl,
                        String.class
                );

        if (response == null
                || response.isBlank()) {

            throw new RuntimeException(
                    "Empty response from Open-Meteo."
            );
        }

        return response;
    }
}