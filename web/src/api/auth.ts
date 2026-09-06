import { apiPost } from "./client";
import { apiGet } from "./client";

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  id: number;
  username: string;
  role: "ADMIN" | "ANALYST";
};

export function login(request: LoginRequest): Promise<LoginResponse> {
  return apiPost<LoginResponse>("/api/auth/login", request);
}

export function getCurrentUser(): Promise<LoginResponse> {
  return apiGet<LoginResponse>("/api/auth/me");
}

export function logout(): Promise<void> {
  return apiPost<void>("/api/auth/logout");
}
