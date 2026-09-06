import { useState } from "react";
import type { SubmitEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { ApiError } from "../api/client";

function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();
  const { login } = useAuth();

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    try {
      await login({ username, password });
      navigate("/");
    } catch (exception) {
      if (exception instanceof ApiError && exception.status === 401) {
        setError("Invalid username or password.");
        return;
      }

      if (exception instanceof ApiError && exception.status === 403) {
        setError("Login request was rejected.");
        return;
      }

      setError("Unable to sign in.");
    }
  }

  return (
    <main>
      <h1>Sentinel</h1>
      <h2>Analyst Login</h2>

      <form onSubmit={handleSubmit}>
        <label>
          Username
          <input
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />
        </label>

        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        <button type="submit">Sign in</button>

        {error && <p>{error}</p>}
      </form>
    </main>
  );
}

export default LoginPage;
