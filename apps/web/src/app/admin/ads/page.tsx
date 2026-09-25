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
import { publicApi } from "@/lib/api/public";
import type { AdminAdvertisement, Category } from "@/lib/types";
import { Pencil, Plus, Search, Trash2 } from "lucide-react";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

const placementLabels: Record<string, string> = { HOME_BANNER: "Home Banner", HOME_FEED: "Home Feed", CATEGORY_FEED: "Category Feed", NEWSPAPER: "Newspaper" };
function statusClass(status: string) { return status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : status === "PAUSED" || status === "SCHEDULED" ? "bg-amber-50 text-amber-700" : status === "EXPIRED" ? "bg-slate-100 text-slate" : "bg-mist text-slate"; }
function label(value: string) { return value[0] + value.slice(1).toLowerCase(); }

function AdsContent() {
  const { token, hasPermission } = useAuth();
  const [items, setItems] = useState<AdminAdvertisement[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [placement, setPlacement] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const load = useCallback(async () => {
    if (!token) return;
    const params = new URLSearchParams({ page: String(page), limit: "25" });
    if (search) params.set("search", search);
    if (status) params.set("status", status);
    if (placement) params.set("placement", placement);
    if (categoryId) params.set("categoryId", categoryId);
    setLoading(true); setError("");
    try { const response = await adminApi.advertisements(token, params.toString()); setItems(response.items); setHasMore(response.hasMore); }
    catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not load advertisements."); }
    finally { setLoading(false); }
  }, [categoryId, page, placement, search, status, token]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => { if (!token) return; publicApi.categories().then((roots) => setCategories(roots.flatMap((root, index) => [{ id: root.id, name: root.name, slug: root.slug, status: "ACTIVE" as const, displayOrder: index }, ...(root.children ?? []).map((child, childIndex) => ({ id: child.id, name: `${root.name} / ${child.name}`, slug: child.slug, status: "ACTIVE" as const, displayOrder: childIndex }))]))).catch(() => undefined); }, [token]);

  async function remove(item: AdminAdvertisement) {
    if (!token || !hasPermission("AD_DELETE") || !window.confirm("Delete advertisement? This will remove it from administration.")) return;
    try { await adminApi.deleteAdvertisement(token, item.id); setNotice("Advertisement deleted."); await load(); }
    catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not delete the advertisement."); }
  }

  return <AdminShell><div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end"><PageHeader eyebrow="Advertising" title="Advertisements" description="Manage simple, editorially scheduled campaigns across the public platform." />{hasPermission("AD_CREATE") ? <Link href="/admin/ads/new"><Button><Plus size={16} /> Create ad</Button></Link> : null}</div><div className="mt-7 grid gap-3 md:grid-cols-[minmax(0,1fr)_170px_170px_220px]"><div className="relative"><Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate" size={17} /><input value={search} onChange={(event) => { setPage(0); setSearch(event.target.value); }} className="min-h-12 w-full rounded-xl border border-line bg-white pl-11 pr-4 text-sm outline-none focus:border-coral" placeholder="Search advertiser or campaign" aria-label="Search advertisements" /></div><select value={status} onChange={(event) => { setPage(0); setStatus(event.target.value); }} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral" aria-label="Filter advertisements by status"><option value="">All statuses</option><option value="DRAFT">Draft</option><option value="SCHEDULED">Scheduled</option><option value="ACTIVE">Active</option><option value="PAUSED">Paused</option><option value="EXPIRED">Expired</option></select><select value={placement} onChange={(event) => { setPage(0); setPlacement(event.target.value); }} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral" aria-label="Filter advertisements by placement"><option value="">All placements</option>{Object.entries(placementLabels).map(([value, text]) => <option key={value} value={value}>{text}</option>)}</select><select value={categoryId} onChange={(event) => { setPage(0); setCategoryId(event.target.value); }} className="min-h-12 rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral" aria-label="Filter advertisements by category"><option value="">All categories</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></div>{notice ? <div className="mt-6"><Alert type="success">{notice}</Alert></div> : null}{error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}{loading ? <LoadingState label="Loading advertisements" /> : <Card className="mt-7 overflow-hidden"><div className="hidden grid-cols-[minmax(0,1fr)_170px_130px_190px] gap-4 border-b border-line bg-mist/40 px-6 py-3 text-[10px] font-bold uppercase tracking-[0.16em] text-slate sm:grid"><span>Advertiser / campaign</span><span>Placement</span><span>Status</span><span>Actions</span></div>{items.length === 0 ? <div className="px-6 py-12 text-center text-sm text-slate">No advertisements match these filters.</div> : items.map((item) => <div key={item.id} className="grid gap-3 border-b border-line px-6 py-5 last:border-0 sm:grid-cols-[minmax(0,1fr)_170px_130px_190px] sm:items-center"><div><Link href={`/admin/ads/${item.id}`} className="font-semibold text-ink hover:text-coral">{item.title}</Link><p className="mt-1 text-xs text-slate">{item.advertiserName} · {item.mediaType.toLowerCase()}</p></div><p className="text-sm text-slate">{item.placements.map((target) => target.type === "CATEGORY_FEED" ? `${target.categoryName ?? "Category"} feed` : placementLabels[target.type]).join(", ")}</p><Badge className={statusClass(item.status)}>{label(item.status)}</Badge><div className="flex flex-wrap items-center gap-1.5 text-sm text-slate"><Link href={`/admin/ads/${item.id}`} aria-label={`Edit ${item.title}`} title="Edit" className="grid h-9 w-9 place-items-center rounded-lg hover:bg-mist"><Pencil size={15} /></Link>{hasPermission("AD_DELETE") ? <button type="button" onClick={() => void remove(item)} aria-label={`Delete ${item.title}`} title="Delete" className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-red-50 hover:text-red-700"><Trash2 size={15} /></button> : null}</div></div>)}</Card>}{!loading && (page > 0 || hasMore) ? <div className="mt-5 flex justify-end gap-3"><Button type="button" disabled={page === 0} onClick={() => setPage((value) => value - 1)} className="bg-white !text-ink ring-1 ring-line hover:bg-mist">Previous</Button><Button type="button" disabled={!hasMore} onClick={() => setPage((value) => value + 1)}>Next</Button></div> : null}</AdminShell>;
}

export default function AdvertisementsPage() { return <AdminRoute><PermissionRoute permission="AD_VIEW_ADMIN"><AdsContent /></PermissionRoute></AdminRoute>; }
