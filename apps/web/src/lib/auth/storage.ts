import type { User } from "@/lib/types";

const TOKEN_KEY = "news_platform_access_token";
const USER_KEY = "news_platform_user";

export function readAuth(): { token: string | null; user: User | null } {
  if (typeof window === "undefined") return { token: null, user: null };
  const token = window.sessionStorage.getItem(TOKEN_KEY);
  const serializedUser = window.sessionStorage.getItem(USER_KEY);

  try {
    return { token, user: serializedUser ? JSON.parse(serializedUser) as User : null };
  } catch {
    clearAuth();
    return { token: null, user: null };
  }
}

export function writeAuth(token: string, user: User) {
  window.sessionStorage.setItem(TOKEN_KEY, token);
  window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuth() {
  window.sessionStorage.removeItem(TOKEN_KEY);
  window.sessionStorage.removeItem(USER_KEY);
}
