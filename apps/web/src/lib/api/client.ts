import type { ApiError } from "@/lib/types";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080").replace(/\/$/, "");

export function apiUrl(path: string) { return `${API_BASE_URL}${path}`; }

export class ApiClientError extends Error {
  status: number;
  code?: string;
  errors?: Record<string, string>;

  constructor(error: ApiError, status: number) {
    super(error.message);
    this.name = "ApiClientError";
    this.status = status;
    this.code = error.code;
    this.errors = error.errors;
  }
}

export async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers);
  if (!(options.body instanceof FormData)) headers.set("Content-Type", "application/json");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(apiUrl(path), {
      ...options,
      headers,
      cache: "no-store",
    });
  } catch {
    throw new ApiClientError({ message: "The API is unavailable. Please try again." }, 0);
  }

  const responseText = await response.text();
  let body: unknown = null;
  if (responseText) {
    try {
      body = JSON.parse(responseText);
    } catch {
      body = null;
    }
  }

  if (!response.ok) {
    const responseError = body && typeof body === "object" ? body as Partial<ApiError> : {};
    const error: ApiError = {
      message: response.status >= 500
        ? "The service is temporarily unavailable. Please try again."
        : typeof responseError.message === "string" ? responseError.message : "Something went wrong. Please try again.",
      code: typeof responseError.code === "string" ? responseError.code : undefined,
      errors: responseError.errors,
    };
    throw new ApiClientError(error, response.status);
  }

  return body as T;
}

export async function requestBlob(path: string, token: string): Promise<Blob> {
  const headers = new Headers({ Authorization: `Bearer ${token}` });
  let response: Response;
  try {
    response = await fetch(apiUrl(path), { headers, cache: "no-store" });
  } catch {
    throw new ApiClientError({ message: "The API is unavailable. Please try again." }, 0);
  }
  if (!response.ok) {
    let body: Partial<ApiError> = {};
    try { body = await response.json() as Partial<ApiError>; } catch { /* ignore malformed error bodies */ }
    throw new ApiClientError({ message: typeof body.message === "string" ? body.message : "Could not load the newspaper." }, response.status);
  }
  return response.blob();
}
