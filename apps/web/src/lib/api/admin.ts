import { request } from "@/lib/api/client";
import type { AdminRole, PermissionGroup, StaffUser, User } from "@/lib/types";

export const adminApi = {
  me: (token: string) => request<User>("/api/v1/admin/me", {}, token),
  roles: (token: string) => request<AdminRole[]>("/api/v1/admin/roles", {}, token),
  role: (token: string, id: string) => request<AdminRole>(`/api/v1/admin/roles/${id}`, {}, token),
  createRole: (token: string, payload: { name: string; code: string; description: string; permissionCodes: string[] }) =>
    request<AdminRole>("/api/v1/admin/roles", { method: "POST", body: JSON.stringify(payload) }, token),
  updateRole: (token: string, id: string, payload: { name: string; code: string; description: string; permissionCodes: string[]; status: string }) =>
    request<AdminRole>(`/api/v1/admin/roles/${id}`, { method: "PUT", body: JSON.stringify(payload) }, token),
  updateRolePermissions: (token: string, id: string, permissionCodes: string[]) =>
    request<void>(`/api/v1/admin/roles/${id}/permissions`, { method: "PUT", body: JSON.stringify({ permissionCodes }) }, token),
  deleteRole: (token: string, id: string) => request<void>(`/api/v1/admin/roles/${id}`, { method: "DELETE" }, token),
  permissions: (token: string) => request<PermissionGroup[]>("/api/v1/admin/permissions", {}, token),
  staff: (token: string, search?: string) => request<StaffUser[]>(`/api/v1/admin/staff${search ? `?search=${encodeURIComponent(search)}` : ""}`, {}, token),
  staffUser: (token: string, id: string) => request<StaffUser>(`/api/v1/admin/staff/${id}`, {}, token),
  createStaff: (token: string, payload: { name: string; email: string; roleIds: string[] }) =>
    request<StaffUser>("/api/v1/admin/staff", { method: "POST", body: JSON.stringify(payload) }, token),
  updateStaff: (token: string, id: string, payload: { name: string; email: string }) =>
    request<StaffUser>(`/api/v1/admin/staff/${id}`, { method: "PUT", body: JSON.stringify(payload) }, token),
  updateStaffRoles: (token: string, id: string, roleIds: string[]) =>
    request<StaffUser>(`/api/v1/admin/staff/${id}/roles`, { method: "PUT", body: JSON.stringify({ roleIds }) }, token),
  disableStaff: (token: string, id: string) => request<StaffUser>(`/api/v1/admin/staff/${id}/disable`, { method: "POST" }, token),
  enableStaff: (token: string, id: string) => request<StaffUser>(`/api/v1/admin/staff/${id}/enable`, { method: "POST" }, token),
  completeStaffSetup: (payload: { token: string; password: string }) =>
    request<void>("/api/v1/auth/staff-setup", { method: "POST", body: JSON.stringify(payload) }),
};
