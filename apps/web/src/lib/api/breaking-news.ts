import { request } from "@/lib/api/client";
import type { AdminBreakingNewsResponse, AdminStory, PublicBreakingNewsResponse } from "@/lib/types";

type BreakingPayload = {
  breakingUntil?: string;
  untilManuallyRemoved?: boolean;
};

export const breakingNewsApi = {
  listPublic: (limit = 10) => request<PublicBreakingNewsResponse>(`/api/v1/breaking-news?limit=${limit}`),
  listAdmin: (token: string, status: "ACTIVE" | "EXPIRED" | "ALL" = "ACTIVE") => request<AdminBreakingNewsResponse>(`/api/v1/admin/breaking-news?status=${status}`, {}, token),
  enable: (token: string, storyId: string, payload: BreakingPayload = {}) => request<AdminStory>(`/api/v1/admin/stories/${storyId}/breaking`, { method: "POST", body: JSON.stringify(payload) }, token),
  updateExpiry: (token: string, storyId: string, payload: BreakingPayload) => request<AdminStory>(`/api/v1/admin/stories/${storyId}/breaking`, { method: "POST", body: JSON.stringify(payload) }, token),
  disable: (token: string, storyId: string) => request<AdminStory>(`/api/v1/admin/stories/${storyId}/breaking`, { method: "DELETE" }, token),
};
