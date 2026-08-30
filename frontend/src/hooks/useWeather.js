import { useState } from "react";

import {
  searchCity,
  getWeather,
} from "../services/weatherService";

export const useWeather = () => {
  const [weather, setWeather] = useState(null);

  const [location, setLocation] = useState(null);

  const [suggestions, setSuggestions] = useState([]);

  const [loading, setLoading] = useState(false);

  const [searchingLocations, setSearchingLocations] =
    useState(false);

  const [error, setError] = useState("");

  /*
   * Search locations
   */
  const searchLocations = async (city) => {
    if (!city.trim()) {
      setSuggestions([]);
      return;
    }

    try {
      setSearchingLocations(true);

      setError("");

      const results = await searchCity(city);

      setSuggestions(results);
    } catch (err) {
      console.error(err);

      setSuggestions([]);

      setError(
        "Unable to search locations. Please try again."
      );
    } finally {
      setSearchingLocations(false);
    }
  };

  /*
   * Get weather for selected location
   */
  const searchWeather = async (selectedLocation) => {
    if (!selectedLocation) {
      return;
    }

    try {
      setLoading(true);

      setError("");

      const data = await getWeather(
        selectedLocation.latitude,
        selectedLocation.longitude
      );

      setWeather(data);

      setLocation(selectedLocation);

      setSuggestions([]);
    } catch (err) {
      console.error(err);

      setError(
        "Unable to get weather data. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return {
    weather,

    location,

    suggestions,

    loading,

    searchingLocations,

    error,

    searchLocations,

    searchWeather,
  };
};