import { getWeatherInfo } from "../utils/weatherUtils";

function HourlyForecast({ hourly }) {
  if (!hourly || !hourly.time) {
    return null;
  }

  const getHour = (dateString, index) => {
    if (index === 0) {
      return "Now";
    }

    const date = new Date(dateString);

    return date.toLocaleTimeString("en-US", {
      hour: "numeric",
      hour12: true,
    });
  };

  return (
    <section className="hourly-card">

      <div className="hourly-header">
        <p className="forecast-label">
          HOURLY
        </p>

        <h2>
          Hourly Forecast
        </h2>
      </div>

      <div className="hourly-scroll">

        {hourly.time
          .slice(0, 12)
          .map((time, index) => {

            const weatherInfo =
              getWeatherInfo(
                hourly.weather_code[index]
              );

            return (
              <div
                className="hourly-item"
                key={time}
              >

                <p className="hourly-time">
                  {getHour(time, index)}
                </p>

                <div className="hourly-icon">
                  {weatherInfo.icon}
                </div>

                <p className="hourly-temp">
                  {Math.round(
                    hourly.temperature_2m[index]
                  )}
                  °
                </p>

                <p className="hourly-rain">
                  💧{" "}
                  {hourly
                    .precipitation_probability[
                      index
                    ] ?? 0}
                  %
                </p>

              </div>
            );
          })}

      </div>

    </section>
  );
}

export default HourlyForecast;