import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { PageHeader } from "@/components/ui/PageHeader";
import { StoryForm } from "@/components/admin/StoryForm";

export default function NewStoryPage() { return <AdminRoute><PermissionRoute permission="STORY_CREATE"><AdminShell><div className="border-b border-line pb-8"><PageHeader eyebrow="Content / Stories" title="Create story" description="Start a draft, add reporting and media, then publish when it is ready." /></div><div className="mt-8"><StoryForm /></div></AdminShell></PermissionRoute></AdminRoute>; }
