import { request } from "@/lib/api/client";
import type { AdminComment, AdminCommentPage, ModerationRequest } from "@/lib/types";

export const moderationApi = {
  list: (token: string, params = "") => request<AdminCommentPage>(`/api/v1/admin/comments${params ? `?${params}` : ""}`, {}, token),
  get: (token: string, id: string) => request<AdminComment>(`/api/v1/admin/comments/${id}`, {}, token),
  hide: (token: string, id: string, payload: ModerationRequest) => request<AdminComment>(`/api/v1/admin/comments/${id}/hide`, { method: "POST", body: JSON.stringify(payload) }, token),
  restore: (token: string, id: string) => request<AdminComment>(`/api/v1/admin/comments/${id}/restore`, { method: "POST" }, token),
  delete: (token: string, id: string) => request<AdminComment>(`/api/v1/admin/comments/${id}`, { method: "DELETE" }, token),
};
