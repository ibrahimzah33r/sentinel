import { useEffect, useState } from "react";
import { ApiError } from "../api/client";
import {
  createAnalyst,
  deleteAnalyst,
  getAnalysts,
  resetAnalystPassword,
  setAnalystEnabled,
  setAnalystRole,
} from "../api/admin";

import type { AnalystResponse } from "../api/admin";

function AdminPage() {
  const [analysts, setAnalysts] = useState<AnalystResponse[]>([]);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [resetPassword, setResetPassword] = useState("");
  const [resetAnalystId, setResetAnalystId] = useState<number | null>(null);

  const [error, setError] = useState<string | null>(null);

  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const enabledAdminCount = analysts.filter(
    (analyst) => analyst.role === "ADMIN" && analyst.enabled,
  ).length;

  useEffect(() => {
    loadAnalysts();
  }, []);

  async function loadAnalysts() {
    try {
      setError(null);

      const response = await getAnalysts();

      setAnalysts(response);
    } catch {
      setError("Unable to load analysts.");
    }
  }

  async function handleCreateAnalyst() {
    if (!username.trim() || !password) {
      return;
    }

    try {
      setError(null);
      setActionLoading("create");

      const createdAnalyst = await createAnalyst({
        username: username.trim(),
        password,
      });

      setAnalysts((currentAnalysts) => [
        ...currentAnalysts,
        createdAnalyst,
      ]);

      setUsername("");
      setPassword("");
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        setError("That username already exists.");
        return;
      }

      setError("Unable to create analyst.");
    } finally {
      setActionLoading(null);
    }
  }

  async function handleEnabledChange(
    analyst: AnalystResponse,
  ) {
    try {
      setError(null);
      setActionLoading(`enabled-${analyst.id}`);

      const updatedAnalyst = await setAnalystEnabled(
        analyst.id,
        !analyst.enabled,
      );

      setAnalysts((currentAnalysts) =>
        currentAnalysts.map((currentAnalyst) =>
          currentAnalyst.id === updatedAnalyst.id
            ? updatedAnalyst
            : currentAnalyst,
        ),
      );
    } catch (error) {
      if (
        error instanceof ApiError
        && error.status === 400
      ) {
        setError(
          "The last enabled admin cannot be disabled.",
        );
        return;
      }

      setError("Unable to update analyst.");
    } finally {
      setActionLoading(null);
    }
  }

  async function handleResetPassword() {
    if (resetAnalystId === null || !resetPassword) {
      return;
    }

    try {
      setError(null);
      setActionLoading(`reset-${resetAnalystId}`);

      await resetAnalystPassword(resetAnalystId, {
        password: resetPassword,
      });

      setResetAnalystId(null);
      setResetPassword("");
    } catch {
      setError("Unable to reset password.");
    } finally {
      setActionLoading(null);
    }
  }

  async function handleRoleChange(
    analyst: AnalystResponse,
  ) {
    const newRole =
      analyst.role === "ADMIN"
        ? "ANALYST"
        : "ADMIN";

    try {
      setError(null);
      setActionLoading(`role-${analyst.id}`);

      const updatedAnalyst = await setAnalystRole(
        analyst.id,
        newRole,
      );

      setAnalysts((currentAnalysts) =>
        currentAnalysts.map((currentAnalyst) =>
          currentAnalyst.id === updatedAnalyst.id
            ? updatedAnalyst
            : currentAnalyst,
        ),
      );
    } catch (error) {
      if (
        error instanceof ApiError
        && error.status === 400
      ) {
        setError(
          "The last enabled admin cannot be demoted.",
        );
        return;
      }

      setError("Unable to change analyst role.");
    } finally {
      setActionLoading(null);
    }
  }

  async function handleDeleteAnalyst(
    analyst: AnalystResponse,
  ) {
    const confirmed = window.confirm(
      `Delete ${analyst.username}? This cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setError(null);
      setActionLoading(`delete-${analyst.id}`);

      await deleteAnalyst(analyst.id);

      setAnalysts((currentAnalysts) =>
        currentAnalysts.filter(
          (currentAnalyst) =>
            currentAnalyst.id !== analyst.id,
        ),
      );
    } catch (error) {
      if (
        error instanceof ApiError
        && error.status === 400
      ) {
        setError(
          "The last enabled admin cannot be deleted.",
        );
        return;
      }

      setError("Unable to delete analyst.");
    } finally {
      setActionLoading(null);
    }
  }

  return (
    <section>
      <h2>Admin</h2>
      <p>Manage Sentinel analyst accounts.</p>

      {error && <p role="alert">{error}</p>}

      <section>
        <h3>Add analyst</h3>

        <input
          type="text"
          value={username}
          placeholder="Username"
          onChange={(event) =>
            setUsername(event.target.value)
          }
        />

        <input
          type="password"
          value={password}
          placeholder="Password"
          onChange={(event) =>
            setPassword(event.target.value)
          }
        />

        <button
          type="button"
          onClick={handleCreateAnalyst}
          disabled={
            !username.trim()
            || !password
            || actionLoading === "create"
          }
        >
          {actionLoading === "create"
            ? "Creating..."
            : "Add analyst"}
        </button>
      </section>

      <section>
        <h3>Analysts</h3>

        {analysts.map((analyst) => {
          const isLastEnabledAdmin =
            analyst.role === "ADMIN"
            && analyst.enabled
            && enabledAdminCount <= 1;

          const isResetting =
            resetAnalystId === analyst.id;

          return (
            <div
              key={analyst.id}
              className="analyst-card"
            >
              <div className="analyst-meta">
                <strong>
                  {analyst.username}
                </strong>

                <span>{analyst.role}</span>

                <span>
                  {analyst.enabled
                    ? "Enabled"
                    : "Disabled"}
                </span>
              </div>

              <div className="analyst-actions">
                <button
                  type="button"
                  onClick={() => {
                    setResetAnalystId(analyst.id);
                    setResetPassword("");
                  }}
                >
                  Reset password
                </button>

                <button
                  type="button"
                  onClick={() =>
                    handleRoleChange(analyst)
                  }
                  disabled={
                    actionLoading ===
                      `role-${analyst.id}`
                    || isLastEnabledAdmin
                  }
                  title={
                    isLastEnabledAdmin
                      ? "Cannot demote the last enabled admin."
                      : undefined
                  }
                >
                  {actionLoading ===
                  `role-${analyst.id}`
                    ? "Updating role..."
                    : analyst.role === "ADMIN"
                      ? "Demote to analyst"
                      : "Promote to admin"}
                </button>

                <button
                  type="button"
                  onClick={() =>
                    handleEnabledChange(analyst)
                  }
                  disabled={
                    actionLoading ===
                      `enabled-${analyst.id}`
                    || isLastEnabledAdmin
                  }
                  title={
                    isLastEnabledAdmin
                      ? "Cannot disable the last enabled admin."
                      : undefined
                  }
                >
                  {actionLoading ===
                  `enabled-${analyst.id}`
                    ? analyst.enabled
                      ? "Disabling..."
                      : "Enabling..."
                    : analyst.enabled
                      ? "Disable account"
                      : "Enable account"}
                </button>

                <button
                  type="button"
                  onClick={() =>
                    handleDeleteAnalyst(analyst)
                  }
                  disabled={
                    actionLoading ===
                      `delete-${analyst.id}`
                    || isLastEnabledAdmin
                  }
                  title={
                    isLastEnabledAdmin
                      ? "Cannot delete the last enabled admin."
                      : undefined
                  }
                >
                  {actionLoading ===
                  `delete-${analyst.id}`
                    ? "Deleting..."
                    : "Delete account"}
                </button>
              </div>

              {isResetting && (
                <div className="reset-password-form">
                  <input
                    type="password"
                    value={resetPassword}
                    placeholder="New password"
                    onChange={(event) =>
                      setResetPassword(
                        event.target.value,
                      )
                    }
                  />

                  <button
                    type="button"
                    onClick={handleResetPassword}
                    disabled={
                      !resetPassword
                      || actionLoading ===
                        `reset-${analyst.id}`
                    }
                  >
                    {actionLoading ===
                    `reset-${analyst.id}`
                      ? "Resetting..."
                      : "Confirm reset"}
                  </button>

                  <button
                    type="button"
                    onClick={() => {
                      setResetAnalystId(null);
                      setResetPassword("");
                    }}
                  >
                    Cancel
                  </button>
                </div>
              )}

              {isLastEnabledAdmin && (
                <p className="analyst-protection-message">
                  This is the last enabled admin.
                  Add or enable another admin before
                  disabling, demoting, or deleting
                  this account.
                </p>
              )}
            </div>
          );
        })}
      </section>
    </section>
  );
}

export default AdminPage;