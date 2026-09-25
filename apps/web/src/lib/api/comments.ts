import { request } from "@/lib/api/client";
import type { Comment, CommentEngagement, CommentPage, CreateCommentRequest, UpdateCommentRequest } from "@/lib/types";

export const commentsApi = {
  list: (storyId: string, params = "", token?: string) => request<CommentPage>(`/api/v1/stories/${storyId}/comments${params ? `?${params}` : ""}`, {}, token),
  create: (token: string, storyId: string, payload: CreateCommentRequest) => request<Comment>(`/api/v1/stories/${storyId}/comments`, { method: "POST", body: JSON.stringify(payload) }, token),
  update: (token: string, commentId: string, payload: UpdateCommentRequest) => request<Comment>(`/api/v1/comments/${commentId}`, { method: "PUT", body: JSON.stringify(payload) }, token),
  deleteOwn: (token: string, commentId: string) => request<void>(`/api/v1/comments/${commentId}`, { method: "DELETE" }, token),
  like: (token: string, commentId: string) => request<CommentEngagement>(`/api/v1/comments/${commentId}/like`, { method: "POST" }, token),
  unlike: (token: string, commentId: string) => request<CommentEngagement>(`/api/v1/comments/${commentId}/like`, { method: "DELETE" }, token),
};
