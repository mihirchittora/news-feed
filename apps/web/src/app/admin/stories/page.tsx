"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { BreakingBadge } from "@/components/public/BreakingBadge";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminStory, Category } from "@/lib/types";
import { Eye, Pencil, Plus, Search, Trash2 } from "lucide-react";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function StoriesContent() {
  const { token, hasPermission } = useAuth();
  const [stories, setStories] = useState<AdminStory[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [breaking, setBreaking] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const load = useCallback(async () => {
    if (!token) return;
    const params = new URLSearchParams();
    if (search) params.set("search", search);
    if (status) params.set("status", status);
    if (categoryId) params.set("categoryId", categoryId);
    if (breaking) params.set("breaking", breaking);
    setLoading(true);
    setError("");
    try {
      setStories(await adminApi.stories(token, params.toString()));
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not load stories.");
    } finally {
      setLoading(false);
    }
  }, [breaking, categoryId, search, status, token]);

  useEffect(() => {
    if (!token) return;
    adminApi.categories(token).then(setCategories).catch(() => undefined);
  }, [token]);

  useEffect(() => {
    void load();
  }, [load]);

  async function changePublication(story: AdminStory) {
    if (!token || !hasPermission("STORY_PUBLISH")) return;
    try {
      if (story.status === "PUBLISHED") {
        await adminApi.unpublishStory(token, story.id);
        setNotice("Story unpublished.");
      } else {
        await adminApi.publishStory(token, story.id);
        setNotice("Story published.");
      }
      await load();
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not update publication status.");
    }
  }

  async function removeStory(story: AdminStory) {
    if (!token || !hasPermission("STORY_DELETE") || !window.confirm(`Delete “${story.title}”?`)) return;
    try {
      await adminApi.deleteStory(token, story.id);
      setNotice("Story deleted.");
      await load();
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not delete story.");
    }
  }

  return <AdminShell>
    <div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end">
      <PageHeader eyebrow="Content" title="Stories" description="Create, review, and publish the newsroom’s reporting." />
      {hasPermission("STORY_CREATE") ? <Link href="/admin/stories/new" className="inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-xl bg-ink px-4 text-sm font-semibold text-white hover:bg-ink/90"><Plus size={16} /> Create story</Link> : null}
    </div>
    <div className="mt-7 grid gap-3 md:grid-cols-[minmax(0,1fr)_180px_180px_220px]">
      <div className="relative"><Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate" size={17} /><input value={search} onChange={(event) => setSearch(event.target.value)} className="min-h-12 w-full rounded-xl border border-line bg-white pl-11 pr-4 text-sm outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="Search stories" aria-label="Search stories" /></div>
      <select value={status} onChange={(event) => setStatus(event.target.value)} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral" aria-label="Filter by status"><option value="">All statuses</option><option value="DRAFT">Draft</option><option value="PUBLISHED">Published</option><option value="UNPUBLISHED">Unpublished</option></select>
      <select value={breaking} onChange={(event) => setBreaking(event.target.value)} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral" aria-label="Filter by Breaking News"><option value="">All Breaking states</option><option value="true">Breaking</option><option value="false">Not Breaking</option></select>
      <select value={categoryId} onChange={(event) => setCategoryId(event.target.value)} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral" aria-label="Filter by category"><option value="">All categories</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select>
    </div>
    {notice ? <div className="mt-6"><Alert type="success">{notice}</Alert></div> : null}
    {error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}
    {loading ? <LoadingState label="Loading stories" /> : <Card className="mt-7 overflow-hidden">
      <div className="hidden grid-cols-[minmax(0,1fr)_140px_120px_190px] gap-4 border-b border-line bg-mist/40 px-6 py-3 text-[10px] font-bold uppercase tracking-[0.16em] text-slate sm:grid"><span>Story</span><span>Category</span><span>Status</span><span>Actions</span></div>
      {stories.length === 0 ? <div className="px-6 py-12 text-center text-sm text-slate">No stories match these filters.</div> : stories.map((story) => <div key={story.id} className="grid gap-3 border-b border-line px-6 py-5 last:border-0 sm:grid-cols-[minmax(0,1fr)_140px_120px_190px] sm:items-center">
        <div><div className="flex flex-wrap items-center gap-2"><Link href={`/admin/stories/${story.id}`} className="font-semibold text-ink hover:text-coral">{story.title}</Link>{story.breakingActive ? <BreakingBadge compact /> : story.isBreaking ? <Badge className="bg-amber-50 text-amber-700">Breaking expired</Badge> : null}</div><p className="mt-1 text-xs text-slate">By {story.authorName}</p></div>
        <p className="text-sm text-slate">{story.categoryName ?? "—"}</p>
        <Badge className={story.status === "PUBLISHED" ? "bg-emerald-50 text-emerald-700" : story.status === "UNPUBLISHED" ? "bg-amber-50 text-amber-700" : "bg-mist text-slate"}>{story.status[0] + story.status.slice(1).toLowerCase()}</Badge>
        <div className="flex flex-wrap items-center gap-1.5 text-sm text-slate">
          <Link href={`/admin/stories/${story.id}`} aria-label={`Edit ${story.title}`} title="Edit" className="grid h-9 w-9 place-items-center rounded-lg hover:bg-mist"><Pencil size={15} /></Link>
          <Link href={`/admin/stories/${story.id}?preview=1`} aria-label={`Preview ${story.title}`} title="Preview" className="grid h-9 w-9 place-items-center rounded-lg hover:bg-mist"><Eye size={15} /></Link>
          {hasPermission("STORY_PUBLISH") ? <Button type="button" onClick={() => void changePublication(story)} className="min-h-9 px-3 py-1 text-xs">{story.status === "PUBLISHED" ? "Unpublish" : "Publish"}</Button> : null}
          {hasPermission("STORY_DELETE") ? <button type="button" onClick={() => void removeStory(story)} aria-label={`Delete ${story.title}`} title="Delete" className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-red-50 hover:text-red-700"><Trash2 size={15} /></button> : null}
        </div>
      </div>)}
    </Card>}
  </AdminShell>;
}

export default function StoriesPage() {
  return <AdminRoute><PermissionRoute permission="STORY_VIEW_ADMIN"><StoriesContent /></PermissionRoute></AdminRoute>;
}
