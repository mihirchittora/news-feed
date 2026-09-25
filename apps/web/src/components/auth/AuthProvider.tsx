"use client";

import { authApi } from "@/lib/api/auth";
import { ApiClientError } from "@/lib/api/client";
import { userApi } from "@/lib/api/user";
import { clearAuth, readAuth, writeAuth } from "@/lib/auth/storage";
import { hasAllPermissions, hasAnyPermission, hasPermission } from "@/lib/auth/permissions";
import type { LoginRequest, RegisterRequest, Role, User } from "@/lib/types";
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

type AuthContextValue = {
  user: User | null;
  token: string | null;
  authenticated: boolean;
  role: Role | null;
  isLoading: boolean;
  loading: boolean;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  hasAllPermissions: (permissions: string[]) => boolean;
  login: (payload: LoginRequest) => Promise<User>;
  register: (payload: RegisterRequest) => ReturnType<typeof authApi.register>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const stored = readAuth();
    if (!stored.token || !stored.user) {
      setIsLoading(false);
      return;
    }

    userApi.me(stored.token)
      .then((currentUser) => {
        writeAuth(stored.token as string, currentUser);
        setToken(stored.token);
        setUser(currentUser);
      })
      .catch((error) => {
        if (error instanceof ApiClientError && error.status === 0) {
          setToken(stored.token);
          setUser(stored.user);
        } else {
          clearAuth();
        }
      })
      .finally(() => setIsLoading(false));
  }, []);

  const login = useCallback(async (payload: LoginRequest) => {
    const response = await authApi.login(payload);
    writeAuth(response.accessToken, response.user);
    setToken(response.accessToken);
    setUser(response.user);
    return response.user;
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    return authApi.register(payload);
  }, []);

  const logout = useCallback(() => {
    clearAuth();
    setToken(null);
    setUser(null);
  }, []);

  const can = useCallback((permission: string) => hasPermission(user, permission), [user]);
  const canAny = useCallback((permissions: string[]) => hasAnyPermission(user, permissions), [user]);
  const canAll = useCallback((permissions: string[]) => hasAllPermissions(user, permissions), [user]);

  const value = useMemo(() => ({
    user,
    token,
    authenticated: Boolean(user),
    role: user?.role ?? null,
    hasPermission: can,
    hasAnyPermission: canAny,
    hasAllPermissions: canAll,
    isLoading,
    loading: isLoading,
    login,
    register,
    logout,
  }), [user, token, isLoading, login, register, logout, can, canAny, canAll]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}
