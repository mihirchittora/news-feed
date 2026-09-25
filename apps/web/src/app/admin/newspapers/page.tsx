"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminNewspaper } from "@/lib/types";
import { Plus, Search } from "lucide-react";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function formatDate(value: string) { return new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { day: "2-digit", month: "short", year: "numeric" }); }

function NewspapersContent() {
  const { token } = useAuth();
  const [items, setItems] = useState<AdminNewspaper[]>([]);
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => { if (!token) return; adminApi.newspapers(token).then(setItems).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load newspapers.")).finally(() => setLoading(false)); }, [token]);
  const filtered = useMemo(() => items.filter((item) => (!query.trim() || `${item.title} ${item.edition}`.toLowerCase().includes(query.trim().toLowerCase())) && (status === "ALL" || item.status === status)), [items, query, status]);
  return <AdminShell><div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end"><PageHeader eyebrow="Publishing" title="Newspapers" description="Upload, review, and publish protected daily editions." /><Link href="/admin/newspapers/new"><Button><Plus size={16} /> Upload</Button></Link></div>{error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}<div className="mt-7 flex flex-col gap-3 sm:flex-row"><label className="relative flex-1"><span className="sr-only">Search newspapers</span><Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate" /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search title or edition" className="min-h-11 w-full rounded-xl border border-line bg-white pl-11 pr-4 text-sm outline-none focus:border-coral" /></label><select value={status} onChange={(event) => setStatus(event.target.value)} className="min-h-11 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral"><option value="ALL">All statuses</option><option value="DRAFT">Draft</option><option value="PUBLISHED">Published</option><option value="UNPUBLISHED">Unpublished</option></select></div>{loading ? <LoadingState label="Loading newspapers" /> : <Card className="mt-5 overflow-hidden"><div className="hidden grid-cols-[1fr_130px_130px_100px] gap-4 border-b border-line bg-mist/40 px-6 py-3 text-[10px] font-bold uppercase tracking-[0.16em] text-slate sm:grid"><span>Date / edition</span><span>Status</span><span>Files</span><span /></div>{filtered.length ? filtered.map((item) => <div key={item.id} className="grid gap-3 border-b border-line px-6 py-5 last:border-0 sm:grid-cols-[1fr_130px_130px_100px] sm:items-center"><div><p className="font-semibold text-ink">{formatDate(item.editionDate)}</p><p className="mt-1 text-sm text-slate">{item.edition} · {item.title}</p></div><Badge className={item.status === "PUBLISHED" ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"}>{item.status}</Badge><p className="text-sm text-slate">{item.hasDocument ? "PDF" : "No PDF"}{item.hasCover ? " · Cover" : ""}</p><Link href={`/admin/newspapers/${item.id}`} className="text-sm font-bold text-ink underline decoration-coral decoration-2 underline-offset-4">Edit</Link></div>) : <div className="px-6 py-12 text-center text-sm text-slate">No editions match this filter.</div>}</Card>}</AdminShell>;
}

export default function NewspapersPage() { return <AdminRoute><PermissionRoute permission="NEWSPAPER_VIEW_ADMIN"><NewspapersContent /></PermissionRoute></AdminRoute>; }
