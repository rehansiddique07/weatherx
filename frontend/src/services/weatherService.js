import axios from "axios";

const WEATHER_API =
  "https://api.open-meteo.com/v1/forecast";

const GEOCODING_API =
  "https://geocoding-api.open-meteo.com/v1/search";

/*
 * Search cities
 */
export const searchCity = async (city) => {
  const response = await axios.get(GEOCODING_API, {
    params: {
      name: city,
      count: 8,
      language: "en",
      format: "json",
    },
  });

  return response.data.results || [];
};

/*
 * Get weather
 */
export const getWeather = async (
  latitude,
  longitude
) => {
  const response = await axios.get(WEATHER_API, {
    params: {
      latitude,
      longitude,

      current:
        "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,visibility",

      hourly:
        "temperature_2m,weather_code,relative_humidity_2m,precipitation_probability",

      daily:
        "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",

      forecast_days: 7,

      timezone: "auto",
    },
  });

  return response.data;
};