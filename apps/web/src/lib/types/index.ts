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
};

export type FeedResponse = { items: StorySummary[]; nextCursor?: string | null; hasMore: boolean };
