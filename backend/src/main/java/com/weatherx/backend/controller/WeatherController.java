package com.weatherx.backend.controller;

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

            String geoUrl =
                    "https://geocoding-api.open-meteo.com/v1/search"
                    + "?name=" + city
                    + "&count=1"
                    + "&language=en"
                    + "&format=json";

            String locationResponse =
                    restTemplate.getForObject(
                            geoUrl,
                            String.class
                    );

            if (locationResponse == null
                    || !locationResponse.contains("\"latitude\"")) {

                throw new RuntimeException(
                        "City not found"
                );
            }

            double latitude = extractNumber(
                    locationResponse,
                    "\"latitude\":"
            );

            double longitude = extractNumber(
                    locationResponse,
                    "\"longitude\":"
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

        return restTemplate.getForObject(
                weatherUrl,
                String.class
        );
    }

    private double extractNumber(
            String json,
            String key
    ) {

        int start = json.indexOf(key);

        if (start == -1) {

            throw new RuntimeException(
                    "Could not find " + key
            );
        }

        start += key.length();

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