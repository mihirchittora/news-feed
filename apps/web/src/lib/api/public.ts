import { request } from "@/lib/api/client";
import type { FeedResponse, PublicCategory, PublicStory } from "@/lib/types";

export const publicApi = {
  categories: () => request<PublicCategory[]>("/api/v1/categories"),
  feed: (params = "") => request<FeedResponse>(`/api/v1/feed${params ? `?${params}` : ""}`),
  story: (slug: string) => request<PublicStory>(`/api/v1/feed/${encodeURIComponent(slug)}`),
};
