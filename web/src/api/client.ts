const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

type CsrfTokenResponse = {
  token: string;
};

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function throwApiError(response: Response): Promise<never> {
  let message = `Request failed with status ${response.status}`;

  try {
    const body = await response.text();

    if (body) {
      message = body;
    }
  } catch {
    // Keep the default message.
  }

  throw new ApiError(response.status, message);
}

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: "include",
  });

  if (!response.ok) {
    await throwApiError(response);
  }

  return response.json() as Promise<T>;
}

export async function apiPost<T>(path: string, body?: unknown): Promise<T> {
  const csrfToken = await getCsrfToken();

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "X-CSRF-TOKEN": csrfToken,
      ...(body
        ? {
            "Content-Type": "application/json",
          }
        : {}),
    },
    credentials: "include",
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    await throwApiError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function apiPatch<T>(path: string, body?: unknown): Promise<T> {
  const csrfToken = await getCsrfToken();

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "PATCH",
    headers: {
      "X-CSRF-TOKEN": csrfToken,
      ...(body
        ? {
            "Content-Type": "application/json",
          }
        : {}),
    },
    credentials: "include",
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    await throwApiError(response);
  }

  return response.json() as Promise<T>;
}

async function getCsrfToken(): Promise<string> {
  const response = await fetch(`${API_BASE_URL}/api/auth/csrf`, {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error(`CSRF request failed with status ${response.status}`);
  }

  const data = (await response.json()) as CsrfTokenResponse;

  return data.token;
}

export async function apiDelete<T>(path: string): Promise<T> {
  const csrfToken = await getCsrfToken();

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "DELETE",
    headers: {
      "X-CSRF-TOKEN": csrfToken,
    },
    credentials: "include",
  });

  if (!response.ok) {
    await throwApiError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}
