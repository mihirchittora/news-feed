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
import { breakingNewsApi } from "@/lib/api/breaking-news";
import { ApiClientError } from "@/lib/api/client";
import type { AdminBreakingNewsItem } from "@/lib/types";
import { Eye, PlusCircle, Trash2 } from "lucide-react";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function formatDate(value?: string | null) {
  return value ? new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "Until manually removed";
}

function BreakingNewsContent() {
  const { token } = useAuth();
  const [status, setStatus] = useState<"ACTIVE" | "EXPIRED" | "ALL">("ACTIVE");
  const [items, setItems] = useState<AdminBreakingNewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState("");
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      setItems((await breakingNewsApi.listAdmin(token, status)).items);
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not load Breaking News.");
    } finally {
      setLoading(false);
    }
  }, [status, token]);

  useEffect(() => { void load(); }, [load]);

  async function extend(item: AdminBreakingNewsItem) {
    if (!token) return;
    setBusyId(item.id);
    setError("");
    try {
      await breakingNewsApi.updateExpiry(token, item.id, { breakingUntil: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString() });
      setNotice(`${item.title} is active for another two hours.`);
      await load();
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not extend Breaking News.");
    } finally {
      setBusyId("");
    }
  }

  async function remove(item: AdminBreakingNewsItem) {
    if (!token || !window.confirm(`Remove Breaking News from “${item.title}”?`)) return;
    setBusyId(item.id);
    setError("");
    try {
      await breakingNewsApi.disable(token, item.id);
      setNotice("Breaking News status removed.");
      await load();
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not remove Breaking News.");
    } finally {
      setBusyId("");
    }
  }

  return <AdminShell>
    <div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end"><PageHeader eyebrow="Content" title="Breaking News" description="Review active and recently expired editorial alerts." /><Link href="/admin/stories" className="inline-flex min-h-11 items-center justify-center gap-2 rounded-xl bg-ink px-4 text-sm font-semibold text-white hover:bg-ink/90"><PlusCircle size={16} /> Choose a story</Link></div>
    <div className="mt-7 flex flex-wrap items-center justify-between gap-3"><div className="flex rounded-xl border border-line bg-white p-1"><button type="button" onClick={() => setStatus("ACTIVE")} className={`rounded-lg px-4 py-2 text-sm font-semibold ${status === "ACTIVE" ? "bg-ink text-white" : "text-slate hover:bg-mist"}`}>Active</button><button type="button" onClick={() => setStatus("EXPIRED")} className={`rounded-lg px-4 py-2 text-sm font-semibold ${status === "EXPIRED" ? "bg-ink text-white" : "text-slate hover:bg-mist"}`}>Expired</button><button type="button" onClick={() => setStatus("ALL")} className={`rounded-lg px-4 py-2 text-sm font-semibold ${status === "ALL" ? "bg-ink text-white" : "text-slate hover:bg-mist"}`}>All</button></div></div>
    {notice ? <div className="mt-6"><Alert type="success">{notice}</Alert></div> : null}
    {error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}
    {loading ? <LoadingState label="Loading Breaking News" /> : <Card className="mt-7 divide-y divide-line overflow-hidden">
      {!items.length ? <div className="px-6 py-12 text-center text-sm text-slate">No {status.toLowerCase()} Breaking News stories.</div> : items.map((item) => <div key={item.id} className="flex flex-col gap-4 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0"><div className="flex flex-wrap items-center gap-2">{item.active ? <BreakingBadge compact /> : <Badge className="bg-amber-50 text-amber-700">Expired</Badge>}<Link href={`/admin/stories/${item.id}`} className="font-semibold text-ink hover:text-coral">{item.title}</Link></div><p className="mt-2 text-sm text-slate">{item.categoryName ?? "Uncategorized"} · Started {formatDate(item.breakingStartedAt)}</p><p className="mt-1 text-xs text-slate">{item.active ? `Ends ${formatDate(item.breakingUntil)}` : `Expired ${formatDate(item.breakingUntil)}`}</p></div>
        <div className="flex shrink-0 flex-wrap gap-2"><Link href={`/admin/stories/${item.id}`} className="inline-flex min-h-9 items-center gap-2 rounded-lg border border-line px-3 text-xs font-semibold text-ink hover:bg-mist"><Eye size={14} /> View story</Link><Button type="button" loading={busyId === item.id} onClick={() => void extend(item)} className="min-h-9 px-3 py-1 text-xs">Extend</Button><button type="button" disabled={busyId === item.id} onClick={() => void remove(item)} className="inline-flex min-h-9 items-center gap-2 rounded-lg px-3 text-xs font-semibold text-red-700 hover:bg-red-50"><Trash2 size={14} /> Remove</button></div>
      </div>)}
    </Card>}
  </AdminShell>;
}

export default function BreakingNewsPage() {
  return <AdminRoute><PermissionRoute permission="BREAKING_NEWS_MANAGE"><BreakingNewsContent /></PermissionRoute></AdminRoute>;
}
