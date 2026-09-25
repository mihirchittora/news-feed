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
  requestId?: string;
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
  parentId?: string | null;
  parentName?: string | null;
};

export type PublicCategory = Pick<Category, "id" | "name" | "slug"> & { description?: string | null; children?: PublicCategory[] };

export type Newspaper = {
  id: string;
  title: string;
  edition: string;
  editionDate: string;
  coverImageUrl?: string | null;
  publishedAt?: string | null;
};

export type AdminNewspaper = Newspaper & {
  status: "DRAFT" | "PUBLISHED" | "UNPUBLISHED" | string;
  hasDocument: boolean;
  hasCover: boolean;
  uploadedBy: string;
  createdAt: string;
  updatedAt: string;
};

export type PublicNewspaperList = { items: Newspaper[] };

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

export type AdvertisementPlacement = {
  type: "HOME_BANNER" | "HOME_FEED" | "CATEGORY_FEED" | "NEWSPAPER" | string;
  categoryId?: string | null;
  categoryName?: string | null;
};

export type Advertisement = {
  id: string;
  title: string;
  advertiserName: string;
  description?: string | null;
  mediaType: "IMAGE" | "VIDEO" | string;
  mediaUrl: string;
  thumbnailUrl?: string | null;
  destinationUrl?: string | null;
  placementType?: string;
};

export type AdminAdvertisement = Advertisement & {
  mimeType: string;
  fileSize: number;
  startAt: string;
  endAt: string;
  status: "DRAFT" | "SCHEDULED" | "ACTIVE" | "PAUSED" | "EXPIRED" | string;
  placements: AdvertisementPlacement[];
  createdBy: string;
  createdAt: string;
  updatedAt: string;
};

export type AdminAdvertisementList = { items: AdminAdvertisement[]; page: number; limit: number; hasMore: boolean };

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

export type DashboardPeriodType = "TODAY" | "LAST_7_DAYS" | "LAST_30_DAYS";

export type DashboardResponse = {
  period: { type: DashboardPeriodType; from: string; to: string; timezone: string };
  content: {
    publishedStories: number;
    draftStories: number;
    unpublishedStories: number;
    activeBreakingNews: number | null;
  } | null;
  engagement: { likes: number; comments: number; commentLikes: number } | null;
  moderation: { hiddenComments: number; moderatedComments: number } | null;
  users: { totalRegisteredUsers: number; newUsers: number } | null;
  staff: { activeStaff: number; disabledStaff: number; pendingSetupStaff: number } | null;
  newspaper: {
    todayPublished: boolean;
    publishedEditionCount: number;
    draftEditionCount: number;
    unpublishedEditionCount: number;
    editions: Array<{
      editionId: string;
      editionName: string;
      title: string;
      status: string;
      publishedAt?: string | null;
    }>;
  } | null;
  advertising: {
    active: number;
    scheduled: number;
    paused: number;
    expired: number;
    expiringSoon: number;
    expiringAds: Array<{
      advertisementId: string;
      advertiserName: string;
      title: string;
      placement: string;
      endAt: string;
    }>;
  } | null;
  attention: {
    items: Array<{
      key: string;
      label: string;
      detail: string;
      severity: "INFO" | "WARNING" | string;
      href: string;
    }>;
  };
  topStories: Array<{
    storyId: string;
    title: string;
    category: string;
    publishedAt?: string | null;
    likeCount: number;
    commentCount: number;
    commentLikeCount: number;
    engagementCount: number;
  }> | null;
  categoryBreakdown: Array<{ category: string; published: number }> | null;
  publishingTrend: Array<{ date: string; published: number }> | null;
  recentActivity: Array<{
    activityId: string;
    action: string;
    targetType: string;
    targetLabel: string;
    actorName?: string | null;
    createdAt: string;
  }>;
};
