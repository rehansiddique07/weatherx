import { getWeatherInfo } from "../utils/weatherUtils";

function DailyForecast({ daily }) {
  if (!daily || !daily.time) {
    return null;
  }

  const getDay = (dateString, index) => {
    if (index === 0) {
      return "Today";
    }

    const date = new Date(dateString);

    return date.toLocaleDateString("en-US", {
      weekday: "short",
    });
  };

  return (
    <section className="forecast-card">

      <div className="forecast-header">

        <div>
          <p className="forecast-label">
            FORECAST
          </p>

          <h2>
            7-Day Forecast
          </h2>
        </div>

      </div>


      <div className="daily-list">

        {daily.time.map((date, index) => {

          const weatherInfo =
            getWeatherInfo(
              daily.weather_code[index]
            );

          return (
            <div
              className="daily-item"
              key={date}
            >

              <div className="forecast-day">
                {getDay(date, index)}
              </div>


              <div className="forecast-condition">

                <span className="forecast-icon">
                  {weatherInfo.icon}
                </span>

                <span>
                  {weatherInfo.label}
                </span>

              </div>


              <div className="rain-probability">
                💧{" "}
                {daily
                  .precipitation_probability_max[
                    index
                  ] ?? 0}
                %
              </div>


              <div className="forecast-temperature">

                <strong>
                  {Math.round(
                    daily.temperature_2m_max[
                      index
                    ]
                  )}
                  °
                </strong>

                <span>
                  {Math.round(
                    daily.temperature_2m_min[
                      index
                    ]
                  )}
                  °
                </span>

              </div>

            </div>
          );
        })}

      </div>

    </section>
  );
}

export default DailyForecast;