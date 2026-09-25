import { request } from "@/lib/api/client";
import type { CommentPage, FeedResponse, PublicCategory, PublicNewspaperList, PublicStory } from "@/lib/types";

export const publicApi = {
  categories: () => request<PublicCategory[]>("/api/v1/categories"),
  category: (slug: string) => request<PublicCategory>(`/api/v1/categories/${encodeURIComponent(slug)}`),
  newspapers: () => request<PublicNewspaperList>("/api/v1/newspapers"),
  newspaper: (id: string) => request<import("@/lib/types").Newspaper>(`/api/v1/newspapers/${id}`),
  feed: (params = "", token?: string) => request<FeedResponse>(`/api/v1/feed${params ? `?${params}` : ""}`, {}, token),
  story: (slug: string, token?: string) => request<PublicStory>(`/api/v1/feed/${encodeURIComponent(slug)}`, {}, token),
  comments: (storyId: string, params = "", token?: string) => request<CommentPage>(`/api/v1/stories/${storyId}/comments${params ? `?${params}` : ""}`, {}, token),
};
