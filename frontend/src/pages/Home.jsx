import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import API_URL from "../config/api";

function Home() {
  const navigate = useNavigate();

  const [city, setCity] = useState("");
  const [weather, setWeather] = useState(null);
  const [hourly, setHourly] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [favoriteWeather, setFavoriteWeather] = useState({});
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [favoriteLoading, setFavoriteLoading] = useState(false);
  const [favoriteWeatherLoading, setFavoriteWeatherLoading] =
    useState(false);

  const user = JSON.parse(
    localStorage.getItem("user") || "{}"
  );

  const userId = user.id;

  // ==============================
  // LOGOUT
  // ==============================

  const handleLogout = () => {
    localStorage.removeItem("isLoggedIn");
    localStorage.removeItem("user");

    navigate("/login", { replace: true });
  };

  // ==============================
  // WEATHER ICON
  // ==============================

  const getWeatherIcon = (code) => {
    if (code === 0) {
      return "☀️";
    }

    if (code >= 1 && code <= 3) {
      return "☁️";
    }

    if (code >= 45 && code <= 48) {
      return "🌫️";
    }

    if (code >= 51 && code <= 67) {
      return "🌧️";
    }

    if (code >= 71 && code <= 77) {
      return "❄️";
    }

    if (code >= 80 && code <= 82) {
      return "🌦️";
    }

    if (code >= 85 && code <= 86) {
      return "🌨️";
    }

    if (code >= 95) {
      return "⛈️";
    }

    return "☁️";
  };

  // ==============================
  // WEATHER DESCRIPTION
  // ==============================

  const getWeatherDescription = (code) => {
    if (code === 0) return "Clear Sky";
    if (code === 1) return "Mainly Clear";
    if (code === 2) return "Partly Cloudy";
    if (code === 3) return "Overcast";

    if (code >= 45 && code <= 48) {
      return "Fog";
    }

    if (code >= 51 && code <= 57) {
      return "Drizzle";
    }

    if (code >= 61 && code <= 67) {
      return "Rain";
    }

    if (code >= 71 && code <= 77) {
      return "Snow";
    }

    if (code >= 80 && code <= 82) {
      return "Rain Showers";
    }

    if (code >= 85 && code <= 86) {
      return "Snow Showers";
    }

    if (code >= 95) {
      return "Thunderstorm";
    }

    return "Unknown";
  };

  // ==============================
  // LOAD WEATHER FOR ALL FAVORITES
  // ==============================

  const loadFavoriteWeather = async (favoriteList) => {
    if (!favoriteList || favoriteList.length === 0) {
      setFavoriteWeather({});
      return;
    }

    try {
      setFavoriteWeatherLoading(true);

      const weatherResults = await Promise.all(
        favoriteList.map(async (favorite) => {
          try {
            const response = await fetch(
              `${API_URL}/api/weather?city=${encodeURIComponent(
                favorite.city
              )}`
            );

            if (!response.ok) {
              throw new Error(
                `Weather unavailable for ${favorite.city}`
              );
            }

            const data = await response.json();

            return {
              city: favorite.city,
              data,
            };
          } catch (err) {
            console.error(
              `Favorite weather error for ${favorite.city}:`,
              err
            );

            return {
              city: favorite.city,
              data: null,
            };
          }
        })
      );

      const weatherMap = {};

      weatherResults.forEach((result) => {
        weatherMap[result.city.toLowerCase()] = result.data;
      });

      setFavoriteWeather(weatherMap);
    } catch (err) {
      console.error(
        "Favorite weather loading error:",
        err
      );
    } finally {
      setFavoriteWeatherLoading(false);
    }
  };

  // ==============================
  // LOAD FAVORITES
  // ==============================

  const loadFavorites = async () => {
    if (!userId) {
      return;
    }

    try {
      const response = await fetch(
        `${API_URL}/api/favorites?userId=${userId}`
      );

      if (!response.ok) {
        throw new Error(
          "Unable to load favorites."
        );
      }

      const data = await response.json();

      setFavorites(data);

      await loadFavoriteWeather(data);
    } catch (err) {
      console.error(
        "Favorites error:",
        err
      );
    }
  };

  // ==============================
  // SEARCH WEATHER
  // ==============================

  const searchWeather = async (searchCity) => {
    const cleanCity = searchCity.trim();

    if (!cleanCity) {
      setError(
        "Please enter a city name."
      );
      return;
    }

    try {
      setLoading(true);
      setError("");

      const response = await fetch(
        `${API_URL}/api/weather?city=${encodeURIComponent(
          cleanCity
        )}`
      );

      if (!response.ok) {
        throw new Error(
          "Unable to get weather."
        );
      }

      const data = await response.json();

      if (!data.current) {
        throw new Error(
          "Weather data not found."
        );
      }

      setWeather(data);
      setCity(cleanCity);

      // ==============================
      // HOURLY FORECAST
      // ==============================

      if (
        data.hourly &&
        data.hourly.time &&
        data.hourly.temperature_2m
      ) {
        const hourlyData =
          data.hourly.time.map(
            (time, index) => ({
              time,

              temperature:
                data.hourly
                  .temperature_2m[index],

              humidity:
                data.hourly
                  .relative_humidity_2m?.[
                  index
                ],

              apparentTemperature:
                data.hourly
                  .apparent_temperature?.[
                  index
                ],

              precipitationProbability:
                data.hourly
                  .precipitation_probability?.[
                  index
                ],

              precipitation:
                data.hourly
                  .precipitation?.[
                  index
                ],

              weatherCode:
                data.hourly
                  .weather_code?.[
                  index
                ],

              windSpeed:
                data.hourly
                  .wind_speed_10m?.[
                  index
                ],
            })
          );

        setHourly(hourlyData);
      } else {
        setHourly([]);
      }
    } catch (err) {
      console.error(
        "Weather error:",
        err
      );

      setWeather(null);
      setHourly([]);

      setError(
        "Unable to find weather for this city."
      );
    } finally {
      setLoading(false);
    }
  };

  // ==============================
  // SEARCH FORM
  // ==============================

  const handleSearch = (event) => {
    event.preventDefault();

    searchWeather(city);
  };

  // ==============================
  // ADD FAVORITE
  // ==============================

  const addFavorite = async () => {
    const cleanCity = city.trim();

    if (!cleanCity) {
      setError(
        "Search for a city first."
      );
      return;
    }

    if (!userId) {
      setError(
        "User session not found."
      );
      return;
    }

    try {
      setFavoriteLoading(true);
      setError("");

      const response = await fetch(
        `${API_URL}/api/favorites?userId=${userId}&city=${encodeURIComponent(
          cleanCity
        )}`,
        {
          method: "POST",
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          typeof data === "string"
            ? data
            : "Unable to add favorite."
        );
      }

      await loadFavorites();
    } catch (err) {
      console.error(
        "Favorite error:",
        err
      );

      setError(
        err.message ||
          "Unable to add favorite city."
      );
    } finally {
      setFavoriteLoading(false);
    }
  };

  // ==============================
  // REMOVE FAVORITE
  // ==============================

  const removeFavorite = async (
    favoriteCity
  ) => {
    if (!userId) {
      return;
    }

    try {
      const response = await fetch(
        `${API_URL}/api/favorites?userId=${userId}&city=${encodeURIComponent(
          favoriteCity
        )}`,
        {
          method: "DELETE",
        }
      );

      if (!response.ok) {
        throw new Error(
          "Unable to remove favorite."
        );
      }

      setFavoriteWeather(
        (previous) => {
          const updated = {
            ...previous,
          };

          delete updated[
            favoriteCity.toLowerCase()
          ];

          return updated;
        }
      );

      await loadFavorites();
    } catch (err) {
      console.error(
        "Remove favorite error:",
        err
      );

      setError(
        "Unable to remove favorite city."
      );
    }
  };

  // ==============================
  // CLICK FAVORITE
  // ==============================

  const openFavorite = (
    favoriteCity
  ) => {
    searchWeather(favoriteCity);
  };

  // ==============================
  // FORMAT HOUR
  // ==============================

  const formatHour = (time) => {
    const date = new Date(time);

    return date.toLocaleTimeString(
      [],
      {
        hour: "numeric",
        minute: "2-digit",
      }
    );
  };

  // ==============================
  // FORMAT DAY
  // ==============================

  const formatDay = (
    dateString
  ) => {
    const date = new Date(
      `${dateString}T12:00:00`
    );

    return date.toLocaleDateString(
      "en-US",
      {
        weekday: "short",
        day: "numeric",
        month: "short",
      }
    );
  };

  // ==============================
  // INITIAL LOAD
  // ==============================

  useEffect(() => {
    searchWeather("Patna");
    loadFavorites();
  }, []);

  // ==============================
  // CHECK FAVORITE
  // ==============================

  const isFavorite =
    favorites.some(
      (favorite) =>
        favorite.city
          .toLowerCase() ===
        city.trim().toLowerCase()
    );

  // ==============================
  // RENDER
  // ==============================

  return (
    <div className="weather-page">

      {/* =========================
          HEADER
      ========================= */}

      <header className="weather-header">

        <div className="weather-logo">
          ☁️ WeatherX
        </div>

        <div className="user-section">

          <span>
            Welcome,{" "}
            <strong>
              {user.name || "User"}
            </strong>
          </span>

          <button
            type="button"
            onClick={handleLogout}
          >
            Logout
          </button>

        </div>

      </header>

      {/* =========================
          MAIN
      ========================= */}

      <main className="weather-container">

        <h1>
          Weather Dashboard
        </h1>

        <p className="weather-subtitle">
          Check current weather conditions
          anywhere in the world
        </p>

        {/* =========================
            SEARCH
        ========================= */}

        <form
          className="weather-search"
          onSubmit={handleSearch}
        >

          <input
            type="text"
            placeholder="Enter city name..."
            value={city}
            onChange={(event) =>
              setCity(
                event.target.value
              )
            }
          />

          <button
            type="submit"
            className="search-button"
            disabled={loading}
          >
            🔍{" "}
            {loading
              ? "Searching..."
              : "Search"}
          </button>

        </form>

        {/* =========================
            FAVORITE BUTTON
        ========================= */}

        {weather && (
          <div className="favorite-action">

            <button
              type="button"
              className="favorite-button"
              onClick={addFavorite}
              disabled={
                favoriteLoading ||
                isFavorite
              }
            >
              {isFavorite
                ? "⭐ Added to Favorites"
                : favoriteLoading
                ? "Adding..."
                : "☆ Add to Favorites"}
            </button>

          </div>
        )}

        {/* =========================
            FAVORITE CITIES
        ========================= */}

        {favorites.length > 0 && (
          <section className="favorites-section">

            <div className="favorites-header">

              <div>
                <h2>
                  ⭐ Favorite Cities
                </h2>

                <p>
                  Live weather for your
                  saved locations
                </p>
              </div>

              <span className="favorites-count">
                {favorites.length}{" "}
                {favorites.length === 1
                  ? "saved"
                  : "saved"}
              </span>

            </div>

            {favoriteWeatherLoading && (
              <div className="favorites-loading">
                <span className="loading-spinner">
                  ⟳
                </span>

                Updating live weather...
              </div>
            )}

            <div className="favorites-grid">

              {favorites.map(
                (favorite) => {

                  const liveWeather =
                    favoriteWeather[
                      favorite.city.toLowerCase()
                    ];

                  const current =
                    liveWeather?.current;

                  return (
                    <div
                      className="favorite-weather-card"
                      key={favorite.id}
                      onClick={() =>
                        openFavorite(
                          favorite.city
                        )
                      }
                    >

                      {/* CARD HEADER */}

                      <div className="favorite-card-header">

                        <div className="favorite-city-info">

                          <h3>
                            {favorite.city}
                          </h3>

                          {liveWeather?.timezone ? (
                            <span>
                              {liveWeather.timezone}
                            </span>
                          ) : (
                            <span>
                              Live weather
                            </span>
                          )}

                        </div>

                        <button
                          type="button"
                          className="remove-favorite"
                          onClick={(event) => {
                            event.stopPropagation();

                            removeFavorite(
                              favorite.city
                            );
                          }}
                          title="Remove from favorites"
                          aria-label={`Remove ${favorite.city} from favorites`}
                        >
                          ×
                        </button>

                      </div>

                      {/* WEATHER PREVIEW */}

                      <div className="favorite-weather-main">

                        <div className="favorite-weather-icon">

                          {current
                            ? getWeatherIcon(
                                current.weather_code
                              )
                            : "☁️"}

                        </div>

                        <div className="favorite-temperature">

                          {current
                            ? Math.round(
                                current.temperature_2m
                              )
                            : "--"}

                          <span>
                            °C
                          </span>

                        </div>

                      </div>

                      {/* CONDITION */}

                      <div className="favorite-condition">

                        {current
                          ? getWeatherDescription(
                              current.weather_code
                            )
                          : "Weather unavailable"}

                      </div>

                      {/* WEATHER DETAILS */}

                      <div className="favorite-details">

                        <div>
                          <span>
                            🌡️
                          </span>

                          <small>
                            Feels Like
                          </small>

                          <strong>
                            {current
                              ? `${current.apparent_temperature}°C`
                              : "--"}
                          </strong>
                        </div>

                        <div>
                          <span>
                            💧
                          </span>

                          <small>
                            Humidity
                          </small>

                          <strong>
                            {current
                              ? `${current.relative_humidity_2m}%`
                              : "--"}
                          </strong>
                        </div>

                        <div>
                          <span>
                            💨
                          </span>

                          <small>
                            Wind
                          </small>

                          <strong>
                            {current
                              ? `${current.wind_speed_10m} km/h`
                              : "--"}
                          </strong>
                        </div>

                      </div>

                      {/* FOOTER */}

                      <div className="favorite-card-footer">

                        <span>
                          ⭐ Saved location
                        </span>

                        <span className="view-weather">
                          View →
                        </span>

                      </div>

                    </div>
                  );
                }
              )}

            </div>

          </section>
        )}

        {/* =========================
            ERROR
        ========================= */}

        {error && (
          <div className="weather-error">
            {error}
          </div>
        )}

        {/* =========================
            CURRENT WEATHER
        ========================= */}

        {weather &&
          weather.current && (
            <>

              <section className="weather-location">

                <h2>
                  {city}
                </h2>

                <p>
                  {weather.timezone}
                </p>

              </section>

              <section className="weather-card">

                <div className="weather-main">

                  <div className="weather-icon">
                    {getWeatherIcon(
                      weather.current
                        .weather_code
                    )}
                  </div>

                  <div>

                    <div className="temperature">
                      {Math.round(
                        weather.current
                          .temperature_2m
                      )}
                      °C
                    </div>

                    <div className="weather-description">
                      {getWeatherDescription(
                        weather.current
                          .weather_code
                      )}
                    </div>

                  </div>

                </div>

                {/* DETAILS */}

                <div className="weather-details">

                  <div className="weather-detail">
                    <span>🌡️</span>
                    <p>Feels Like</p>
                    <strong>
                      {
                        weather.current
                          .apparent_temperature
                      }
                      °C
                    </strong>
                  </div>

                  <div className="weather-detail">
                    <span>💧</span>
                    <p>Humidity</p>
                    <strong>
                      {
                        weather.current
                          .relative_humidity_2m
                      }
                      %
                    </strong>
                  </div>

                  <div className="weather-detail">
                    <span>💨</span>
                    <p>Wind Speed</p>
                    <strong>
                      {
                        weather.current
                          .wind_speed_10m
                      }
                      km/h
                    </strong>
                  </div>

                  <div className="weather-detail">
                    <span>🔽</span>
                    <p>Pressure</p>
                    <strong>
                      {
                        weather.current
                          .surface_pressure
                      }
                      hPa
                    </strong>
                  </div>

                  <div className="weather-detail">
                    <span>🌧️</span>
                    <p>Precipitation</p>
                    <strong>
                      {
                        weather.current
                          .precipitation
                      }
                      mm
                    </strong>
                  </div>

                  <div className="weather-detail">
                    <span>
                      {weather.current.is_day
                        ? "☀️"
                        : "🌙"}
                    </span>

                    <p>
                      Day/Night
                    </p>

                    <strong>
                      {weather.current.is_day
                        ? "Day"
                        : "Night"}
                    </strong>
                  </div>

                </div>

                <p className="weather-updated">
                  Last updated:{" "}
                  {weather.current.time}
                </p>

              </section>

              {/* =========================
                  24 HOUR FORECAST
              ========================= */}

              {hourly.length > 0 && (
                <section className="forecast-section">

                  <h2>
                    24-Hour Forecast
                  </h2>

                  <div className="hourly-forecast">

                    {hourly
                      .slice(0, 24)
                      .map(
                        (hour, index) => (

                          <div
                            className="hourly-card"
                            key={index}
                          >

                            <div className="hourly-time">
                              {index === 0
                                ? "Now"
                                : formatHour(
                                    hour.time
                                  )}
                            </div>

                            <div className="hourly-icon">
                              {getWeatherIcon(
                                hour.weatherCode
                              )}
                            </div>

                            <div className="hourly-temperature">
                              {Math.round(
                                hour.temperature
                              )}
                              °C
                            </div>

                            <div className="hourly-condition">
                              {getWeatherDescription(
                                hour.weatherCode
                              )}
                            </div>

                            <div className="hourly-info">
                              💧{" "}
                              {hour.humidity ??
                                "-"}
                              %
                            </div>

                            <div className="hourly-info">
                              🌧️{" "}
                              {hour.precipitationProbability ??
                                "-"}
                              %
                            </div>

                            <div className="hourly-info">
                              💨{" "}
                              {hour.windSpeed ??
                                "-"}{" "}
                              km/h
                            </div>

                          </div>

                        )
                      )}

                  </div>

                </section>
              )}

              {/* =========================
                  7 DAY FORECAST
              ========================= */}

              {weather.daily && (
                <section className="forecast-section">

                  <h2>
                    7-Day Forecast
                  </h2>

                  <div className="daily-forecast">

                    {weather.daily.time.map(
                      (date, index) => {

                        const code =
                          weather.daily
                            .weather_code[
                            index
                          ];

                        return (
                          <div
                            className="daily-card"
                            key={date}
                          >

                            <h3>
                              {formatDay(
                                date
                              )}
                            </h3>

                            <div className="daily-icon">
                              {getWeatherIcon(
                                code
                              )}
                            </div>

                            <p className="daily-condition">
                              {getWeatherDescription(
                                code
                              )}
                            </p>

                            <div className="daily-temperature">

                              <strong>
                                {Math.round(
                                  weather.daily
                                    .temperature_2m_max[
                                      index
                                    ]
                                )}
                                °
                              </strong>

                              <span>
                                {Math.round(
                                  weather.daily
                                    .temperature_2m_min[
                                      index
                                    ]
                                )}
                                °
                              </span>

                            </div>

                            <div className="daily-rain">
                              🌧️{" "}
                              {
                                weather.daily
                                  .precipitation_probability_max[
                                  index
                                ]
                              }
                              % rain
                            </div>

                            <div className="daily-precipitation">
                              💧{" "}
                              {weather.daily
                                .precipitation_sum[
                                  index
                                ]
                                .toFixed(1)}
                              mm
                            </div>

                          </div>
                        );
                      }
                    )}

                  </div>

                </section>
              )}

            </>
          )}

      </main>

    </div>
  );
}

export default Home;