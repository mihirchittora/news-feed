import type { User } from "@/lib/types";

export function hasPermission(user: User | null, permission: string) {
  if (!user) return false;
  if (user.permissions) return user.permissions.includes(permission);
  return user.role === "ADMIN";
}

export function hasAnyPermission(user: User | null, permissions: string[]) {
  return permissions.some((permission) => hasPermission(user, permission));
}

export function hasAllPermissions(user: User | null, permissions: string[]) {
  return permissions.every((permission) => hasPermission(user, permission));
}
