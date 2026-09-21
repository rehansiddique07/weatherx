package com.weatherx.backend.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${WEATHER_API_KEY}")
    private String weatherApiKey;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping("/api/weather")
    public Map<String, Object> getWeather(
            @RequestParam String city
    ) {

        if (city == null || city.trim().isEmpty()) {
            throw new RuntimeException("City name is required.");
        }

        return fetchWeather(city.trim());
    }

    @GetMapping("/api/weather/location")
    public Map<String, Object> getWeatherByLocation(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {

        return fetchWeather(
                latitude + "," + longitude
        );
    }

    private Map<String, Object> fetchWeather(String query) {

        try {

            var uri = UriComponentsBuilder
                    .fromUriString(
                            "https://api.weatherapi.com/v1/forecast.json"
                    )
                    .queryParam("key", weatherApiKey)
                    .queryParam("q", query)
                    .queryParam("days", 3)
                    .queryParam("aqi", "no")
                    .queryParam("alerts", "no")
                    .build()
                    .encode()
                    .toUri();

            String jsonResponse = restTemplate.getForObject(
                    uri,
                    String.class
            );

            if (jsonResponse == null || jsonResponse.isBlank()) {
                throw new RuntimeException(
                        "Empty response from weather service."
                );
            }

            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root.has("error")) {

                String message = root
                        .path("error")
                        .path("message")
                        .asText("Weather service error.");

                throw new RuntimeException(message);
            }

            return convertToWeatherXFormat(root);

        } catch (HttpStatusCodeException e) {

            throw new RuntimeException(
                    "Weather service returned HTTP "
                            + e.getStatusCode().value()
                            + ": "
                            + e.getResponseBodyAsString()
            );

        } catch (Exception e) {

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new RuntimeException(
                    "Unable to fetch weather data: "
                            + e.getMessage()
            );
        }
    }

    private Map<String, Object> convertToWeatherXFormat(
            JsonNode root
    ) {

        Map<String, Object> response = new HashMap<>();

        JsonNode location = root.path("location");
        JsonNode current = root.path("current");

        Map<String, Object> currentData = new HashMap<>();

        currentData.put(
                "temperature_2m",
                current.path("temp_c").asDouble()
        );

        currentData.put(
                "relative_humidity_2m",
                current.path("humidity").asDouble()
        );

        currentData.put(
                "apparent_temperature",
                current.path("feelslike_c").asDouble()
        );

        currentData.put(
                "is_day",
                current.path("is_day").asInt() == 1
        );

        currentData.put(
                "precipitation",
                current.path("precip_mm").asDouble()
        );

        currentData.put(
                "weather_code",
                mapWeatherCode(
                        current.path("condition")
                                .path("code")
                                .asInt()
                )
        );

        currentData.put(
                "wind_speed_10m",
                current.path("wind_kph").asDouble()
        );

        currentData.put(
                "surface_pressure",
                current.path("pressure_mb").asDouble()
        );

        currentData.put(
                "time",
                current.path("last_updated").asText()
        );

        response.put(
                "current",
                currentData
        );

        response.put(
                "timezone",
                location.path("tz_id").asText()
        );

        response.put(
                "latitude",
                location.path("lat").asDouble()
        );

        response.put(
                "longitude",
                location.path("lon").asDouble()
        );

        response.put(
                "city",
                location.path("name").asText()
        );

        response.put(
                "country",
                location.path("country").asText()
        );

        buildHourlyForecast(
                root,
                response
        );

        return response;
    }

    private void buildHourlyForecast(
            JsonNode root,
            Map<String, Object> response
    ) {

        List<JsonNode> allHours = new ArrayList<>();

        JsonNode forecastDays = root
                .path("forecast")
                .path("forecastday");

        for (JsonNode day : forecastDays) {

            JsonNode hours = day.path("hour");

            for (JsonNode hour : hours) {
                allHours.add(hour);
            }
        }

        allHours.sort(
                Comparator.comparing(
                        hour -> hour.path("time").asText()
                )
        );

        String currentTime = root
                .path("location")
                .path("localtime")
                .asText();

        int startIndex = findStartingHour(
                allHours,
                currentTime
        );

        List<String> times = new ArrayList<>();
        List<Double> temperatures = new ArrayList<>();
        List<Double> humidity = new ArrayList<>();
        List<Double> apparentTemperature = new ArrayList<>();
        List<Integer> precipitationProbability = new ArrayList<>();
        List<Double> precipitation = new ArrayList<>();
        List<Integer> weatherCodes = new ArrayList<>();
        List<Double> windSpeed = new ArrayList<>();

        int endIndex = Math.min(
                startIndex + 24,
                allHours.size()
        );

        for (int i = startIndex; i < endIndex; i++) {

            JsonNode hour = allHours.get(i);

            times.add(
                    hour.path("time").asText()
            );

            temperatures.add(
                    hour.path("temp_c").asDouble()
            );

            humidity.add(
                    hour.path("humidity").asDouble()
            );

            apparentTemperature.add(
                    hour.path("feelslike_c").asDouble()
            );

            precipitationProbability.add(
                    hour.path("chance_of_rain").asInt()
            );

            precipitation.add(
                    hour.path("precip_mm").asDouble()
            );

            weatherCodes.add(
                    mapWeatherCode(
                            hour.path("condition")
                                    .path("code")
                                    .asInt()
                    )
            );

            windSpeed.add(
                    hour.path("wind_kph").asDouble()
            );
        }

        Map<String, Object> hourly = new HashMap<>();

        hourly.put(
                "time",
                times
        );

        hourly.put(
                "temperature_2m",
                temperatures
        );

        hourly.put(
                "relative_humidity_2m",
                humidity
        );

        hourly.put(
                "apparent_temperature",
                apparentTemperature
        );

        hourly.put(
                "precipitation_probability",
                precipitationProbability
        );

        hourly.put(
                "precipitation",
                precipitation
        );

        hourly.put(
                "weather_code",
                weatherCodes
        );

        hourly.put(
                "wind_speed_10m",
                windSpeed
        );

        response.put(
                "hourly",
                hourly
        );
    }

    private int findStartingHour(
            List<JsonNode> hours,
            String currentTime
    ) {

        try {

            LocalDateTime now = LocalDateTime.parse(
                    currentTime,
                    DATE_TIME_FORMATTER
            );

            for (int i = 0; i < hours.size(); i++) {

                LocalDateTime hour = LocalDateTime.parse(
                        hours.get(i)
                                .path("time")
                                .asText(),
                        DATE_TIME_FORMATTER
                );

                if (!hour.isBefore(now)) {
                    return i;
                }
            }

        } catch (Exception ignored) {
        }

        return 0;
    }

    private int mapWeatherCode(int code) {

        return switch (code) {

            case 1000 -> 0;

            case 1003 -> 2;

            case 1006, 1009 -> 3;

            case 1030, 1135 -> 45;

            case 1147 -> 48;

            case 1063,
                 1150,
                 1153,
                 1180,
                 1183,
                 1240 -> 61;

            case 1186,
                 1189,
                 1243 -> 63;

            case 1192,
                 1195,
                 1246 -> 65;

            case 1072,
                 1168 -> 56;

            case 1171 -> 57;

            case 1066,
                 1114,
                 1210,
                 1213,
                 1255 -> 71;

            case 1069,
                 1204,
                 1216,
                 1258 -> 73;

            case 1075,
                 1207,
                 1219,
                 1222,
                 1225 -> 75;

            case 1087,
                 1273,
                 1276,
                 1279,
                 1282 -> 95;

            case 1117 -> 86;

            case 1237,
                 1261,
                 1264 -> 77;

            default -> 3;
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception e
    ) {

        Map<String, Object> error = new HashMap<>();

        error.put(
                "error",
                true
        );

        error.put(
                "message",
                e.getMessage()
        );

        return ResponseEntity
                .status(500)
                .body(error);
    }
}