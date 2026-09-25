"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { LoadingState } from "@/components/ui/LoadingState";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !user) router.replace("/login");
  }, [isLoading, user, router]);

  if (isLoading || !user) return <LoadingState label="Checking your account" />;
  return <>{children}</>;
}

export function AdminRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;
    if (!user) {
      router.replace("/login");
      return;
    }
    if (user.role !== "ADMIN") router.replace("/403");
  }, [isLoading, user, router]);

  if (isLoading || !user || user.role !== "ADMIN") return <LoadingState label="Checking admin access" />;
  return <>{children}</>;
}

export function PermissionRoute({ permission, children }: { permission: string; children: React.ReactNode }) {
  const { user, isLoading, hasPermission } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;
    if (!user) router.replace("/login");
    else if (!hasPermission(permission)) router.replace("/403");
  }, [isLoading, user, hasPermission, permission, router]);

  if (isLoading || !user || !hasPermission(permission)) return <LoadingState label="Checking access" />;
  return <>{children}</>;
}
