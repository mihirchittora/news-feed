export type Role = "USER" | "ADMIN" | string;

export type RoleSummary = {
  id: string;
  code: string;
  name: string;
};

export type User = {
  id: string;
  name: string;
  email: string;
  role: Role;
  roles?: RoleSummary[];
  permissions?: string[];
  status?: "ACTIVE" | "DISABLED" | "PENDING_SETUP" | string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: User;
};

export type ApiError = {
  timestamp?: string;
  status?: number;
  code?: string;
  message: string;
  errors?: Record<string, string>;
  path?: string;
};

export type Permission = {
  code: string;
  name: string;
  description: string;
  category: string;
};

export type PermissionGroup = {
  category: string;
  permissions: Permission[];
};

export type AdminRole = {
  id: string;
  name: string;
  code: string;
  description?: string;
  systemRole: boolean;
  status: "ACTIVE" | "INACTIVE";
  permissionCodes: string[];
};

export type StaffUser = {
  id: string;
  name: string;
  email: string;
  status: "ACTIVE" | "DISABLED" | "PENDING_SETUP" | string;
  roles: RoleSummary[];
  setupLink?: string | null;
};

export type Category = {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  status: "ACTIVE" | "INACTIVE";
  displayOrder: number;
};

export type PublicCategory = Pick<Category, "id" | "name" | "slug"> & { description?: string | null };

export type Tag = { id: string; name: string; slug: string };

export type StoryMedia = {
  id: string;
  type: "IMAGE" | "VIDEO";
  url: string;
  thumbnailUrl?: string | null;
  mimeType: string;
  fileSize: number;
  width?: number | null;
  height?: number | null;
  durationSeconds?: number | null;
  sortOrder: number;
};

export type StorySummary = {
  id: string;
  title: string;
  slug: string;
  summary?: string | null;
  category?: PublicCategory | null;
  tags: Tag[];
  media: StoryMedia[];
  publishedAt?: string | null;
  authorName: string;
  isBreaking: boolean;
  likeCount: number;
  commentCount: number;
  likedByCurrentUser: boolean;
};

export type PublicStory = StorySummary & { body: string };

export type AdminStory = {
  id: string;
  title: string;
  slug: string;
  summary?: string | null;
  body: string;
  status: "DRAFT" | "PUBLISHED" | "UNPUBLISHED";
  categoryId?: string | null;
  categoryName?: string | null;
  tagIds: string[];
  media: StoryMedia[];
  authorId: string;
  authorName: string;
  publishedAt?: string | null;
  createdAt: string;
  updatedAt: string;
  isBreaking: boolean;
  breakingStartedAt?: string | null;
  breakingUntil?: string | null;
  breakingActive: boolean;
};

export type FeedResponse = { items: StorySummary[]; nextCursor?: string | null; hasMore: boolean };

export type BreakingNewsItem = {
  id: string;
  slug: string;
  title: string;
  summary?: string | null;
  category?: PublicCategory | null;
  media: StoryMedia[];
  publishedAt?: string | null;
  breakingStartedAt: string;
  breakingUntil?: string | null;
};

export type PublicBreakingNewsResponse = { items: BreakingNewsItem[] };

export type AdminBreakingNewsItem = {
  id: string;
  slug: string;
  title: string;
  status: "DRAFT" | "PUBLISHED" | "UNPUBLISHED" | string;
  categoryName?: string | null;
  breakingStartedAt: string;
  breakingUntil?: string | null;
  active: boolean;
};

export type AdminBreakingNewsResponse = { items: AdminBreakingNewsItem[] };

export type StoryEngagement = {
  likeCount: number;
  commentCount: number;
  likedByCurrentUser: boolean;
};

export type CommentStatus = "VISIBLE" | "HIDDEN" | "DELETED";

export type CommentAuthor = { id: string; name: string };

export type Comment = {
  id: string;
  body: string;
  author: CommentAuthor;
  parentCommentId: string | null;
  createdAt: string;
  updatedAt: string;
  edited: boolean;
  likeCount: number;
  likedByCurrentUser: boolean;
  ownedByCurrentUser: boolean;
};

export type CommentPage = { items: Comment[]; nextCursor?: string | null; hasMore: boolean };

export type CreateCommentRequest = { body: string; parentCommentId?: string | null };

export type UpdateCommentRequest = { body: string };

export type CommentEngagement = { likeCount: number; likedByCurrentUser: boolean };

export type ModerationRequest = { reason?: string };

export type AdminComment = {
  id: string;
  body: string;
  author: CommentAuthor;
  story: { id: string; title: string; slug: string };
  status: CommentStatus;
  createdAt: string;
  updatedAt: string;
  deletedAt?: string | null;
  moderatedAt?: string | null;
  moderatedBy?: CommentAuthor | null;
  moderationReason?: string | null;
};

export type AdminCommentPage = { items: AdminComment[]; page: number; limit: number; hasMore: boolean };
