import { request } from "@/lib/api/client";
import type { User } from "@/lib/types";

export const userApi = {
  me: (token: string) => request<User>("/api/v1/users/me", {}, token),
};
