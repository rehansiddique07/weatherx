import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import API_URL from "../../config/api";
import "./Login.css";

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event) => {
    event.preventDefault();

    setError("");

    if (!email.trim()) {
      setError("Please enter your email.");
      return;
    }

    if (!password.trim()) {
      setError("Please enter your password.");
      return;
    }

    try {
      setLoading(true);

      console.log("Sending login request...");

      const response = await fetch(
        `${API_URL}/api/auth/login`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: email.trim(),
            password: password,
          }),
        }
      );

      console.log("Backend status:", response.status);

      const contentType = response.headers.get("content-type");

      let data;

      if (contentType && contentType.includes("application/json")) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      console.log("Backend response:", data);

      if (!response.ok) {
        if (typeof data === "string") {
          setError(data);
        } else {
          setError(
            data.message || "Invalid email or password."
          );
        }

        return;
      }

      localStorage.setItem("isLoggedIn", "true");
      localStorage.setItem("user", JSON.stringify(data));

      console.log("Login successful:", data);

      navigate("/", { replace: true });
    } catch (err) {
      console.error("Login error:", err);

      setError(
        "Unable to connect to server. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">

      <div className="weather-decoration decoration-one">
        ☁️
      </div>

      <div className="weather-decoration decoration-two">
        ✦
      </div>

      <div className="weather-decoration decoration-three">
        ☀️
      </div>

      <div className="weather-decoration decoration-four">
        ☁
      </div>

      <main className="login-wrapper">

        <section className="login-card">

          <div className="login-brand">

            <div className="brand-icon">
              ☁️
            </div>

            <div className="brand-name">
              Weather<span>X</span>
            </div>

          </div>

          <div className="login-heading">

            <h1>Welcome Back</h1>

            <p>
              Sign in to continue your weather journey
            </p>

          </div>

          {error && (
            <div className="login-error">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          <form
            className="login-form"
            onSubmit={handleLogin}
          >

            <div className="input-group">

              <label htmlFor="email">
                Email Address
              </label>

              <div className="input-wrapper">

                <span className="input-icon">
                  ✉️
                </span>

                <input
                  id="email"
                  type="email"
                  placeholder="Enter your email"
                  value={email}
                  onChange={(event) =>
                    setEmail(event.target.value)
                  }
                  autoComplete="email"
                  disabled={loading}
                />

              </div>

            </div>

            <div className="input-group">

              <div className="password-label">

                <label htmlFor="password">
                  Password
                </label>

                <span>
                  🔒 Secure Login
                </span>

              </div>

              <div className="input-wrapper">

                <span className="input-icon">
                  🔒
                </span>

                <input
                  id="password"
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(event) =>
                    setPassword(event.target.value)
                  }
                  autoComplete="current-password"
                  disabled={loading}
                />

              </div>

            </div>

            <button
              type="submit"
              className="login-button"
              disabled={loading}
            >

              {loading ? (
                <>
                  <span className="login-spinner"></span>
                  Signing in...
                </>
              ) : (
                <>
                  Sign In
                  <span className="button-arrow">
                    →
                  </span>
                </>
              )}

            </button>

          </form>

          <div className="signup-section">

            <span>
              Don't have an account?
            </span>

            <Link to="/signup">
              Create Account
            </Link>

          </div>

          <Link
            to="/"
            className="back-weather"
          >
            <span>←</span>
            Back to Weather
          </Link>

          <footer className="login-footer">

            <div className="footer-line"></div>

            <div className="powered-by">
              Powered by <strong>Rehan</strong>
            </div>

            <a
              href="https://instagram.com/abstract_mind.io"
              target="_blank"
              rel="noopener noreferrer"
              className="instagram-link"
            >
              ◎ @abstract_mind.io
            </a>

            <p>
              © 2026 WeatherX · Real-time weather made simple
            </p>

          </footer>

        </section>

      </main>

    </div>
  );
}

export default Login;
