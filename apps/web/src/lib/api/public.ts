import { request } from "@/lib/api/client";
import type { CommentPage, FeedResponse, PublicCategory, PublicStory } from "@/lib/types";

export const publicApi = {
  categories: () => request<PublicCategory[]>("/api/v1/categories"),
  feed: (params = "", token?: string) => request<FeedResponse>(`/api/v1/feed${params ? `?${params}` : ""}`, {}, token),
  story: (slug: string, token?: string) => request<PublicStory>(`/api/v1/feed/${encodeURIComponent(slug)}`, {}, token),
  comments: (storyId: string, params = "", token?: string) => request<CommentPage>(`/api/v1/stories/${storyId}/comments${params ? `?${params}` : ""}`, {}, token),
};
