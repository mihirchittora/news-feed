# Authorization matrix

The browser hides navigation items for convenience, but Spring Security method authorization is authoritative. A request without the required authority returns `403` (and an unauthenticated request returns `401`). `SUPER_ADMIN` is an explicit protected authority on every administrative operation and the last active SUPER_ADMIN cannot be disabled or removed from that role.

| Capability / backend operation | Required permission |
| --- | --- |
| View, create, edit, delete stories | `STORY_VIEW_ADMIN`, `STORY_CREATE`, `STORY_EDIT`, `STORY_DELETE` respectively |
| Publish or unpublish stories | `STORY_PUBLISH` |
| Upload story/ad media | `STORY_CREATE` or `STORY_EDIT` or `AD_CREATE` or `AD_EDIT` |
| Manage categories | `CATEGORY_MANAGE` |
| Manage tags | `TAG_MANAGE` |
| View/manage Breaking News | `BREAKING_NEWS_MANAGE` |
| Read public comments | public; hidden/deleted comments are filtered server-side |
| Create/edit/delete own comments | authenticated user; ownership is checked in the service |
| Hide, restore, delete, or review comments | `COMMENT_MODERATE` |
| View newspaper administration | `NEWSPAPER_VIEW_ADMIN` |
| Create/upload or edit newspaper metadata and files | `NEWSPAPER_UPLOAD` / `NEWSPAPER_EDIT` |
| Publish or unpublish newspapers | `NEWSPAPER_PUBLISH` |
| Delete newspapers | `NEWSPAPER_DELETE` |
| Read published newspaper metadata/covers | public |
| Read a published newspaper PDF | authenticated user; publication is checked before the file is resolved |
| View advertising administration | `AD_VIEW_ADMIN` |
| Create or edit advertisements | `AD_CREATE` / `AD_EDIT` |
| Publish advertisements | `AD_PUBLISH` |
| Pause/resume advertisements | `AD_PAUSE` |
| Delete advertisements | `AD_DELETE` |
| View staff | `STAFF_VIEW` |
| Create/edit staff | `STAFF_CREATE` / `STAFF_EDIT` |
| Enable/disable staff | `STAFF_DISABLE` |
| Assign staff roles | `STAFF_ROLE_ASSIGN` |
| View roles and permissions | `ROLE_VIEW` |
| Create/edit/delete roles | `ROLE_CREATE` / `ROLE_EDIT` / `ROLE_DELETE` |
| Assign role permissions | `ROLE_PERMISSION_ASSIGN` |
| Read dashboard | any operational read permission: `STORY_VIEW_ADMIN`, `BREAKING_NEWS_MANAGE`, `COMMENT_MODERATE`, `NEWSPAPER_VIEW_ADMIN`, `AD_VIEW_ADMIN`, `CATEGORY_MANAGE`, `TAG_MANAGE`, `STAFF_VIEW`, or `ROLE_VIEW` |

## Verification notes

- `USER` has no administrative permissions and cannot access `/api/v1/admin/**`, including the dashboard.
- Disabled accounts are rejected during JWT request revalidation, so disabling a staff user invalidates access without waiting for token expiry.
- Removing a permission from a role removes the corresponding authority on the next request; permissions are loaded from the database for each JWT-authenticated request.
- Setup tokens are stored as SHA-256 hashes, expire after 24 hours, and are marked used inside a pessimistic-lock transaction.
