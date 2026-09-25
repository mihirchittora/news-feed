"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { StoryForm } from "@/components/admin/StoryForm";
import { Alert } from "@/components/ui/Alert";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminStory } from "@/lib/types";
import { use } from "react";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function EditStoryContent({ id }: { id: string }) { const { token } = useAuth(); const [story, setStory] = useState<AdminStory | null>(null); const [error, setError] = useState(""); useEffect(() => { if (token) adminApi.story(token, id).then(setStory).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load story.")); }, [token, id]); if (error) return <AdminShell><Alert>{error}</Alert></AdminShell>; if (!story) return <AdminShell><LoadingState label="Loading story" /></AdminShell>; return <AdminShell><div className="border-b border-line pb-8"><PageHeader eyebrow={`Content / Stories / ${story.status}`} title={story.title} description={story.status === "PUBLISHED" && story.publishedAt ? `Published ${new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(story.publishedAt))}` : "Edit this story and manage its publication state."} /></div><div className="mt-8"><StoryForm initialStory={story} /></div></AdminShell>; }
export default function EditStoryPage({ params }: { params: Promise<{ id: string }> }) { const { id } = use(params); return <AdminRoute><PermissionRoute permission="STORY_VIEW_ADMIN"><EditStoryContent id={id} /></PermissionRoute></AdminRoute>; }
