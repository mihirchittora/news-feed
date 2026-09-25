import { request } from "@/lib/api/client";
import type { StoryEngagement } from "@/lib/types";

export const likeApi = {
  likeStory: (token: string, storyId: string) => request<StoryEngagement>(`/api/v1/stories/${storyId}/like`, { method: "POST" }, token),
  unlikeStory: (token: string, storyId: string) => request<StoryEngagement>(`/api/v1/stories/${storyId}/like`, { method: "DELETE" }, token),
};
