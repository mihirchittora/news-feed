"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/components/auth/AuthProvider";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { ApiClientError } from "@/lib/api/client";
import { moderationApi } from "@/lib/api/moderation";
import type { AdminComment } from "@/lib/types";
import { Search, X } from "lucide-react";
import { useCallback, useEffect, useState } from "react";

function dateLabel(value: string) {
  return new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

function statusClass(status: AdminComment["status"]) {
  if (status === "VISIBLE") return "bg-emerald-50 text-emerald-700";
  if (status === "HIDDEN") return "bg-amber-50 text-amber-700";
  return "bg-red-50 text-red-700";
}

function CommentsContent() {
  const { token } = useAuth();
  const [comments, setComments] = useState<AdminComment[]>([]);
  const [status, setStatus] = useState("");
  const [query, setQuery] = useState("");
  const [storyId, setStoryId] = useState("");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [hideTarget, setHideTarget] = useState<AdminComment | null>(null);
  const [reason, setReason] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(async () => {
    if (!token) return;
    const params = new URLSearchParams({ page: String(page), limit: "20" });
    if (status) params.set("status", status);
    if (query.trim()) params.set("query", query.trim());
    if (storyId.trim()) params.set("storyId", storyId.trim());
    setLoading(true);
    setError("");
    try {
      const response = await moderationApi.list(token, params.toString());
      setComments(response.items);
      setHasMore(response.hasMore);
    } catch (failure) {
      setError(failure instanceof ApiClientError ? failure.message : "Could not load comments.");
    } finally {
      setLoading(false);
    }
  }, [page, query, status, storyId, token]);

  useEffect(() => { setPage(0); }, [query, status, storyId]);
  useEffect(() => { void load(); }, [load]);

  async function hideComment() {
    if (!token || !hideTarget) return;
    setActionLoading(true);
    try {
      await moderationApi.hide(token, hideTarget.id, { reason: reason.trim() || undefined });
      setHideTarget(null);
      setReason("");
      setNotice("Comment hidden.");
      await load();
    } catch (failure) {
      setError(failure instanceof ApiClientError ? failure.message : "Could not hide comment.");
    } finally {
      setActionLoading(false);
    }
  }

  async function restoreComment(comment: AdminComment) {
    if (!token || !window.confirm("Restore this comment?")) return;
    setActionLoading(true);
    try {
      await moderationApi.restore(token, comment.id);
      setNotice("Comment restored.");
      await load();
    } catch (failure) {
      setError(failure instanceof ApiClientError ? failure.message : "Could not restore comment.");
    } finally {
      setActionLoading(false);
    }
  }

  async function deleteComment(comment: AdminComment) {
    if (!token || !window.confirm("Delete this comment? It will no longer be publicly visible.")) return;
    setActionLoading(true);
    try {
      await moderationApi.delete(token, comment.id);
      setNotice("Comment deleted.");
      await load();
    } catch (failure) {
      setError(failure instanceof ApiClientError ? failure.message : "Could not delete comment.");
    } finally {
      setActionLoading(false);
    }
  }

  return <AdminShell>
    <div className="border-b border-line pb-8"><PageHeader eyebrow="Content" title="Comments" description="Review and moderate the conversation around published stories." /></div>
    <div className="mt-7 grid gap-3 lg:grid-cols-[180px_minmax(0,1fr)_minmax(0,1fr)]">
      <select value={status} onChange={(event) => setStatus(event.target.value)} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral" aria-label="Filter comments by status"><option value="">All statuses</option><option value="VISIBLE">Visible</option><option value="HIDDEN">Hidden</option><option value="DELETED">Deleted</option></select>
      <div className="relative"><Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate" size={17} /><input value={query} onChange={(event) => setQuery(event.target.value)} className="min-h-12 w-full rounded-xl border border-line bg-white pl-11 pr-4 text-sm outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="Search comment, author, or story" aria-label="Search comments" /></div>
      <input value={storyId} onChange={(event) => setStoryId(event.target.value)} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="Filter by story UUID" aria-label="Filter by story UUID" />
    </div>
    {notice ? <div className="mt-6"><Alert type="success">{notice}</Alert></div> : null}
    {error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}
    {hideTarget ? <div className="fixed inset-0 z-50 grid place-items-center bg-ink/35 px-5"><div role="dialog" aria-modal="true" aria-labelledby="hide-comment-title" className="w-full max-w-lg rounded-3xl border border-line bg-white p-6 shadow-soft"><div className="flex items-start justify-between gap-4"><div><h2 id="hide-comment-title" className="font-display text-2xl font-bold text-ink">Hide comment</h2><p className="mt-2 text-sm leading-6 text-slate">The comment will disappear from the public conversation.</p></div><button type="button" onClick={() => setHideTarget(null)} className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-mist" aria-label="Close hide dialog"><X size={18} /></button></div><label htmlFor="moderation-reason" className="mt-6 block text-sm font-bold text-ink">Reason <span className="font-normal text-slate">(optional)</span></label><textarea id="moderation-reason" value={reason} onChange={(event) => setReason(event.target.value)} maxLength={500} rows={3} className="mt-2 w-full rounded-xl border border-line px-4 py-3 text-sm outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="Spam, abuse, or another moderation reason" /><div className="mt-5 flex justify-end gap-3"><Button type="button" onClick={() => setHideTarget(null)} className="bg-transparent text-ink hover:bg-mist">Cancel</Button><Button type="button" onClick={() => void hideComment()} loading={actionLoading}>Hide comment</Button></div></div></div> : null}
    {loading ? <LoadingState label="Loading comments" /> : <Card className="mt-7 overflow-hidden">
      {comments.length === 0 ? <div className="px-6 py-12 text-center text-sm text-slate">No comments match these filters.</div> : comments.map((comment) => <article key={comment.id} className="border-b border-line px-6 py-6 last:border-0"><div className="flex flex-col justify-between gap-4 sm:flex-row"><div><p className="font-bold text-ink">{comment.author.name}</p><p className="mt-1 text-xs text-slate">{dateLabel(comment.createdAt)}</p><p className="mt-3 text-sm text-slate">Story: <a href={`/story/${comment.story.slug}`} target="_blank" rel="noreferrer" className="font-semibold text-ink hover:text-coral">{comment.story.title}</a></p></div><Badge className={statusClass(comment.status)}>{comment.status[0] + comment.status.slice(1).toLowerCase()}</Badge></div><p className="mt-5 whitespace-pre-wrap rounded-xl bg-mist/50 px-4 py-3 text-sm leading-7 text-slate">{comment.body}</p>{comment.moderationReason ? <p className="mt-3 text-xs text-slate">Reason: <span className="font-semibold text-ink">{comment.moderationReason}</span></p> : null}<div className="mt-4 flex flex-wrap gap-2">{comment.status === "VISIBLE" ? <Button type="button" onClick={() => { setHideTarget(comment); setReason(""); }} disabled={actionLoading} className="min-h-9 px-3 text-xs">Hide</Button> : null}{comment.status === "HIDDEN" ? <Button type="button" onClick={() => void restoreComment(comment)} disabled={actionLoading} className="min-h-9 px-3 text-xs">Restore</Button> : null}{comment.status !== "DELETED" ? <Button type="button" onClick={() => void deleteComment(comment)} disabled={actionLoading} className="min-h-9 bg-transparent px-3 text-xs text-red-700 hover:bg-red-50">Delete</Button> : null}</div></article>)}
    </Card>}
    {!loading && (page > 0 || hasMore) ? <div className="mt-6 flex items-center justify-center gap-3"><Button type="button" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={page === 0}>Previous</Button><span className="text-sm text-slate">Page {page + 1}</span><Button type="button" onClick={() => setPage((current) => current + 1)} disabled={!hasMore}>Next</Button></div> : null}
  </AdminShell>;
}

export default function CommentsPage() {
  return <AdminRoute><PermissionRoute permission="COMMENT_MODERATE"><CommentsContent /></PermissionRoute></AdminRoute>;
}
