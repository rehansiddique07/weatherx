package com.weatherx.backend.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api/weather")
    public String getWeather(@RequestParam String city) {

        try {

            if (city == null || city.trim().isEmpty()) {
                throw new RuntimeException(
                        "City name is required."
                );
            }

            String encodedCity =
                    URLEncoder.encode(
                            city.trim(),
                            StandardCharsets.UTF_8
                    );

            String geoUrl =
                    "https://geocoding-api.open-meteo.com/v1/search"
                    + "?name=" + encodedCity
                    + "&count=1"
                    + "&language=en"
                    + "&format=json";

            System.out.println(
                    "Geocoding URL: " + geoUrl
            );

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
                    || !locationResponse.contains("\"results\"")) {

                throw new RuntimeException(
                        "City not found."
                );
            }

            int resultsIndex =
                    locationResponse.indexOf("\"results\"");

            int latitudeIndex =
                    locationResponse.indexOf(
                            "\"latitude\":",
                            resultsIndex
                    );

            int longitudeIndex =
                    locationResponse.indexOf(
                            "\"longitude\":",
                            resultsIndex
                    );

            if (latitudeIndex == -1
                    || longitudeIndex == -1) {

                throw new RuntimeException(
                        "Could not find city coordinates."
                );
            }

            double latitude =
                    extractNumber(
                            locationResponse,
                            "\"latitude\":",
                            latitudeIndex
                    );

            double longitude =
                    extractNumber(
                            locationResponse,
                            "\"longitude\":",
                            longitudeIndex
                    );

            System.out.println(
                    "Coordinates: "
                            + latitude
                            + ", "
                            + longitude
            );

            return getWeatherData(
                    latitude,
                    longitude
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to find weather for this city: "
                            + e.getMessage()
            );
        }
    }

    @GetMapping("/api/weather/location")
    public String getWeatherByLocation(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {

        return getWeatherData(
                latitude,
                longitude
        );
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
                    "Empty response from weather service."
            );
        }

        return response;
    }

    private double extractNumber(
            String json,
            String key,
            int keyIndex
    ) {

        int start =
                keyIndex + key.length();

        int end = start;

        while (
                end < json.length()
                        && (
                        Character.isDigit(
                                json.charAt(end)
                        )
                        || json.charAt(end) == '.'
                        || json.charAt(end) == '-'
                        )
        ) {

            end++;
        }

        return Double.parseDouble(
                json.substring(start, end)
        );
    }
}