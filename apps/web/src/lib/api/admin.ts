import { request } from "@/lib/api/client";
import type { AdminNewspaper, AdminRole, AdminStory, Category, PermissionGroup, StaffUser, StoryMedia, Tag, User } from "@/lib/types";

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
  categories: (token: string) => request<Category[]>("/api/v1/admin/categories", {}, token),
  category: (token: string, id: string) => request<Category>(`/api/v1/admin/categories/${id}`, {}, token),
  createCategory: (token: string, payload: { name: string; slug?: string; description?: string; status: string; displayOrder: number; parentId?: string | null }) => request<Category>("/api/v1/admin/categories", { method: "POST", body: JSON.stringify(payload) }, token),
  updateCategory: (token: string, id: string, payload: { name: string; slug?: string; description?: string; status: string; displayOrder: number; parentId?: string | null }) => request<Category>(`/api/v1/admin/categories/${id}`, { method: "PUT", body: JSON.stringify(payload) }, token),
  deleteCategory: (token: string, id: string) => request<void>(`/api/v1/admin/categories/${id}`, { method: "DELETE" }, token),
  tags: (token: string, query?: string) => request<Tag[]>(`/api/v1/admin/tags${query ? `?query=${encodeURIComponent(query)}` : ""}`, {}, token),
  createTag: (token: string, payload: { name: string; slug?: string }) => request<Tag>("/api/v1/admin/tags", { method: "POST", body: JSON.stringify(payload) }, token),
  updateTag: (token: string, id: string, payload: { name: string; slug?: string }) => request<Tag>(`/api/v1/admin/tags/${id}`, { method: "PUT", body: JSON.stringify(payload) }, token),
  deleteTag: (token: string, id: string) => request<void>(`/api/v1/admin/tags/${id}`, { method: "DELETE" }, token),
  stories: (token: string, params = "") => request<AdminStory[]>(`/api/v1/admin/stories${params ? `?${params}` : ""}`, {}, token),
  story: (token: string, id: string) => request<AdminStory>(`/api/v1/admin/stories/${id}`, {}, token),
  createStory: (token: string, payload: { title: string; summary?: string; body: string; categoryId?: string; tagIds: string[]; mediaIds: string[] }) => request<AdminStory>("/api/v1/admin/stories", { method: "POST", body: JSON.stringify(payload) }, token),
  updateStory: (token: string, id: string, payload: { title: string; summary?: string; body: string; categoryId?: string; tagIds: string[]; mediaIds: string[] }) => request<AdminStory>(`/api/v1/admin/stories/${id}`, { method: "PUT", body: JSON.stringify(payload) }, token),
  publishStory: (token: string, id: string) => request<AdminStory>(`/api/v1/admin/stories/${id}/publish`, { method: "POST" }, token),
  unpublishStory: (token: string, id: string) => request<AdminStory>(`/api/v1/admin/stories/${id}/unpublish`, { method: "POST" }, token),
  deleteStory: (token: string, id: string) => request<void>(`/api/v1/admin/stories/${id}`, { method: "DELETE" }, token),
  uploadMedia: (token: string, file: File, storyId?: string) => { const body = new FormData(); body.append("file", file); if (storyId) body.append("storyId", storyId); return request<StoryMedia>("/api/v1/admin/media", { method: "POST", body }, token); },
  newspapers: (token: string) => request<AdminNewspaper[]>("/api/v1/admin/newspapers", {}, token),
  newspaper: (token: string, id: string) => request<AdminNewspaper>(`/api/v1/admin/newspapers/${id}`, {}, token),
  createNewspaper: (token: string, payload: { title: string; edition: string; editionDate: string }) => request<AdminNewspaper>("/api/v1/admin/newspapers", { method: "POST", body: JSON.stringify(payload) }, token),
  updateNewspaper: (token: string, id: string, payload: { title: string; edition: string; editionDate: string }) => request<AdminNewspaper>(`/api/v1/admin/newspapers/${id}`, { method: "PUT", body: JSON.stringify(payload) }, token),
  uploadNewspaperDocument: (token: string, id: string, file: File) => { const body = new FormData(); body.append("file", file); return request<AdminNewspaper>(`/api/v1/admin/newspapers/${id}/document`, { method: "POST", body }, token); },
  uploadNewspaperCover: (token: string, id: string, file: File) => { const body = new FormData(); body.append("file", file); return request<AdminNewspaper>(`/api/v1/admin/newspapers/${id}/cover`, { method: "POST", body }, token); },
  publishNewspaper: (token: string, id: string) => request<AdminNewspaper>(`/api/v1/admin/newspapers/${id}/publish`, { method: "POST" }, token),
  unpublishNewspaper: (token: string, id: string) => request<AdminNewspaper>(`/api/v1/admin/newspapers/${id}/unpublish`, { method: "POST" }, token),
  deleteNewspaper: (token: string, id: string) => request<void>(`/api/v1/admin/newspapers/${id}`, { method: "DELETE" }, token),
};
