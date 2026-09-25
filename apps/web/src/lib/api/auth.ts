import { request } from "@/lib/api/client";
import type { AuthResponse, LoginRequest, RegisterRequest, User } from "@/lib/types";

export const authApi = {
  register: (payload: RegisterRequest) =>
    request<User>("/api/v1/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  login: (payload: LoginRequest) =>
    request<AuthResponse>("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};
