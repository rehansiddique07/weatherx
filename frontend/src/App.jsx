import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import Signup from "./pages/auth/Signup";

function App() {
  const checkLogin = () => {
    return localStorage.getItem("isLoggedIn") === "true";
  };

  return (
    <BrowserRouter>
      <Routes>

        {/* Root → Login */}
        <Route
          path="/"
          element={<Navigate to="/login" replace />}
        />

        {/* Login */}
        <Route
          path="/login"
          element={<Login />}
        />

        {/* Signup */}
        <Route
          path="/signup"
          element={<Signup />}
        />

        {/* Protected Home */}
        <Route
          path="/home"
          element={
            checkLogin() ? (
              <Home />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />

        {/* Unknown URL */}
        <Route
          path="*"
          element={<Navigate to="/login" replace />}
        />

      </Routes>
    </BrowserRouter>
  );
}

export default App;