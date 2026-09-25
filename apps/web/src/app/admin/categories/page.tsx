"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { Category } from "@/lib/types";
import { ChevronDown, Pencil, Plus, Trash2, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function CategoriesContent() {
  const { token } = useAuth();
  const [items, setItems] = useState<Category[]>([]);
  const [editing, setEditing] = useState<Category | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [description, setDescription] = useState("");
  const [parentId, setParentId] = useState("");
  const [status, setStatus] = useState("ACTIVE");
  const [displayOrder, setDisplayOrder] = useState("0");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const roots = useMemo(() => items.filter((item) => !item.parentId), [items]);
  const children = useMemo(() => items.filter((item) => item.parentId), [items]);
  const childrenByParent = useMemo(() => children.reduce<Record<string, Category[]>>((groups, item) => {
    const key = item.parentId as string;
    (groups[key] ??= []).push(item);
    return groups;
  }, {}), [children]);

  function load() {
    if (!token) return;
    setLoading(true);
    adminApi.categories(token).then(setItems).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load categories.")).finally(() => setLoading(false));
  }

  useEffect(load, [token]);

  function openForm(category?: Category) {
    setEditing(category ?? null);
    setFormOpen(true);
    setName(category?.name ?? "");
    setSlug(category?.slug ?? "");
    setDescription(category?.description ?? "");
    setParentId(category?.parentId ?? "");
    setStatus(category?.status ?? "ACTIVE");
    setDisplayOrder(String(category?.displayOrder ?? 0));
    setError("");
  }

  function closeForm() { setEditing(null); setFormOpen(false); }

  async function save(event: React.FormEvent) {
    event.preventDefault();
    if (!token) return;
    setSaving(true);
    setError("");
    try {
      const payload = { name, slug: slug || undefined, description, status, displayOrder: Number(displayOrder) || 0, parentId: parentId || null };
      if (editing) await adminApi.updateCategory(token, editing.id, payload);
      else await adminApi.createCategory(token, payload);
      setNotice(editing ? "Category updated." : "Category created.");
      closeForm();
      load();
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not save category.");
    } finally {
      setSaving(false);
    }
  }

  async function remove(category: Category) {
    if (!token || !window.confirm(`Delete ${category.name}?`)) return;
    try {
      await adminApi.deleteCategory(token, category.id);
      setNotice("Category deleted.");
      load();
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not delete category.");
    }
  }

  function row(category: Category, level = 0) {
    const nested = childrenByParent[category.id] ?? [];
    return <div key={category.id}>
      <div className="grid gap-3 border-b border-line px-5 py-4 last:border-0 sm:grid-cols-[1fr_130px_100px_100px] sm:items-center" style={{ paddingLeft: `${20 + level * 28}px` }}>
        <div className="flex items-start gap-2"><div className="mt-0.5 text-coral">{nested.length ? <ChevronDown size={15} aria-hidden="true" /> : <span className="inline-block w-[15px]" />}</div><div><p className="font-semibold text-ink">{category.name}</p><p className="mt-1 font-mono text-xs text-slate">/{category.slug}{category.parentName ? ` · under ${category.parentName}` : ""}</p></div></div>
        <Badge className={category.status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"}>{category.status[0] + category.status.slice(1).toLowerCase()}</Badge>
        <span className="text-sm text-slate">{category.displayOrder}</span>
        <div className="flex gap-1 sm:justify-end"><button type="button" onClick={() => openForm(category)} aria-label={`Edit ${category.name}`} className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-mist hover:text-ink"><Pencil size={15} /></button><button type="button" onClick={() => remove(category)} aria-label={`Delete ${category.name}`} className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-red-50 hover:text-red-700"><Trash2 size={15} /></button></div>
      </div>
      {nested.sort((a, b) => a.displayOrder - b.displayOrder || a.name.localeCompare(b.name)).map((child) => row(child, level + 1))}
    </div>;
  }

  return <AdminShell>
    <div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end"><PageHeader eyebrow="Content" title="Categories" description="Organize the public navigation with a two-level editorial hierarchy." /><Button onClick={() => openForm()}><Plus size={16} /> Add category</Button></div>
    {notice ? <div className="mt-6"><Alert type="success">{notice}</Alert></div> : null}
    {error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}
    {formOpen ? <Card className="mt-7 p-6"><div className="flex items-center justify-between"><div><p className="font-bold text-ink">{editing ? "Edit category" : "Add category"}</p><p className="mt-1 text-sm text-slate">Only top-level categories can have children.</p></div><button type="button" onClick={closeForm} aria-label="Close form" className="text-slate hover:text-ink"><X size={18} /></button></div><form onSubmit={save} className="mt-5 grid gap-4 sm:grid-cols-2"><Input label="Name" value={name} onChange={(event) => setName(event.target.value)} required /><Input label="Slug (optional)" value={slug} onChange={(event) => setSlug(event.target.value)} /><label className="block text-sm font-semibold text-ink">Parent<select value={parentId} onChange={(event) => setParentId(event.target.value)} className="mt-2 min-h-12 w-full rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral"><option value="">None — top level</option>{roots.filter((root) => root.id !== editing?.id).map((root) => <option key={root.id} value={root.id}>{root.name}</option>)}</select></label><Input label="Display order" type="number" min={0} value={displayOrder} onChange={(event) => setDisplayOrder(event.target.value)} /><label className="block text-sm font-semibold text-ink">Status<select value={status} onChange={(event) => setStatus(event.target.value)} className="mt-2 min-h-12 w-full rounded-xl border border-line bg-white px-4 text-sm outline-none focus:border-coral"><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label><label className="block text-sm font-semibold text-ink sm:col-span-2">Description<textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={2} className="mt-2 w-full rounded-xl border border-line bg-white px-4 py-3 text-sm outline-none focus:border-coral" /></label><div className="sm:col-span-2"><Button loading={saving}>{editing ? "Save changes" : "Create category"}</Button></div></form></Card> : null}
    {loading ? <LoadingState label="Loading categories" /> : <Card className="mt-7 overflow-hidden"><div className="hidden grid-cols-[1fr_130px_100px_100px] gap-4 border-b border-line bg-mist/40 px-6 py-3 text-[10px] font-bold uppercase tracking-[0.16em] text-slate sm:grid"><span>Name</span><span>Status</span><span>Order</span><span /></div>{roots.sort((a, b) => a.displayOrder - b.displayOrder || a.name.localeCompare(b.name)).map((root) => row(root))}</Card>}
  </AdminShell>;
}

export default function CategoriesPage() { return <AdminRoute><PermissionRoute permission="CATEGORY_MANAGE"><CategoriesContent /></PermissionRoute></AdminRoute>; }
