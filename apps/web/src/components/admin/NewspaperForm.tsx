"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminNewspaper } from "@/lib/types";
import { useAuth } from "@/components/auth/AuthProvider";
import { useRouter } from "next/navigation";
import { useState } from "react";

export function NewspaperForm({ initialNewspaper }: { initialNewspaper?: AdminNewspaper }) {
  const { token, hasPermission } = useAuth();
  const router = useRouter();
  const isNew = !initialNewspaper;
  const [newspaper, setNewspaper] = useState(initialNewspaper);
  const [title, setTitle] = useState(initialNewspaper?.title ?? "Daily Newspaper");
  const [edition, setEdition] = useState(initialNewspaper?.edition ?? "");
  const [editionDate, setEditionDate] = useState(initialNewspaper?.editionDate ?? new Date().toISOString().slice(0, 10));
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const canUpload = hasPermission("NEWSPAPER_UPLOAD");
  const canEdit = hasPermission("NEWSPAPER_EDIT");
  const canPublish = hasPermission("NEWSPAPER_PUBLISH");
  const canDelete = hasPermission("NEWSPAPER_DELETE");

  async function save(event: React.FormEvent, andPublish = false) {
    event.preventDefault();
    if (!token || (!isNew && !canEdit) || (isNew && !canUpload)) return;
    setSaving(true); setError(""); setNotice("");
    try {
      let saved = isNew ? await adminApi.createNewspaper(token, { title, edition, editionDate }) : await adminApi.updateNewspaper(token, initialNewspaper.id, { title, edition, editionDate });
      if (pdfFile && (canUpload || canEdit)) { saved = await adminApi.uploadNewspaperDocument(token, saved.id, pdfFile); setPdfFile(null); }
      if (coverFile && (canUpload || canEdit)) { saved = await adminApi.uploadNewspaperCover(token, saved.id, coverFile); setCoverFile(null); }
      if (andPublish) { if (!canPublish) { setError("You do not have permission to publish newspapers."); return; } saved = await adminApi.publishNewspaper(token, saved.id); }
      setNewspaper(saved); setNotice(andPublish ? "Newspaper published." : isNew ? "Draft saved." : "Changes saved.");
      if (isNew) router.replace(`/admin/newspapers/${saved.id}`);
    } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not save the newspaper."); } finally { setSaving(false); }
  }

  async function unpublish() { if (!token || !newspaper || !canPublish) return; try { setNewspaper(await adminApi.unpublishNewspaper(token, newspaper.id)); setNotice("Newspaper unpublished."); } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not unpublish the newspaper."); } }
  async function remove() { if (!token || !newspaper || !canDelete || !window.confirm("Delete this newspaper edition and its files?")) return; try { await adminApi.deleteNewspaper(token, newspaper.id); router.replace("/admin/newspapers"); } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not delete the newspaper."); } }

  return <AdminRoute><PermissionRoute permission="NEWSPAPER_VIEW_ADMIN"><div className="max-w-4xl">{error ? <div className="mb-5"><Alert>{error}</Alert></div> : null}{notice ? <div className="mb-5"><Alert type="success">{notice}</Alert></div> : null}<div className="mb-5 flex flex-wrap items-center justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-[0.2em] text-coral">Newspaper</p><h1 className="mt-2 font-display text-4xl font-bold tracking-[-0.05em] text-ink">{isNew ? "Upload newspaper" : "Edit newspaper"}</h1></div>{newspaper ? <Badge className={newspaper.status === "PUBLISHED" ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"}>{newspaper.status}</Badge> : null}</div><form onSubmit={(event) => save(event)}><Card className="grid gap-5 p-6 sm:grid-cols-2"><Input label="Title" value={title} onChange={(event) => setTitle(event.target.value)} required disabled={Boolean(newspaper && !canEdit)} /><Input label="Edition" value={edition} onChange={(event) => setEdition(event.target.value)} required disabled={Boolean(newspaper && !canEdit)} /><Input label="Edition date" type="date" value={editionDate} onChange={(event) => setEditionDate(event.target.value)} required disabled={Boolean(newspaper && !canEdit)} /><div className="sm:col-span-2 rounded-2xl border border-dashed border-line p-5"><p className="text-sm font-bold text-ink">Newspaper PDF</p><p className="mt-1 text-xs text-slate">PDF only, up to the configured server limit. The file is stored outside PostgreSQL.</p><input type="file" accept="application/pdf,.pdf" onChange={(event) => setPdfFile(event.target.files?.[0] ?? null)} disabled={Boolean(newspaper && !canEdit && !canUpload)} className="mt-4 block w-full text-sm text-slate" />{newspaper?.hasDocument ? <p className="mt-2 text-xs font-semibold text-emerald-700">A PDF is attached. Choose a file to replace it.</p> : null}</div><div className="sm:col-span-2 rounded-2xl border border-dashed border-line p-5"><p className="text-sm font-bold text-ink">Cover image <span className="font-normal text-slate">(optional)</span></p><p className="mt-1 text-xs text-slate">JPEG, PNG, or WebP. Cover images are shown on the public archive.</p><input type="file" accept="image/jpeg,image/png,image/webp" onChange={(event) => setCoverFile(event.target.files?.[0] ?? null)} disabled={Boolean(newspaper && !canEdit && !canUpload)} className="mt-4 block w-full text-sm text-slate" />{newspaper?.hasCover ? <p className="mt-2 text-xs font-semibold text-emerald-700">A cover is attached. Choose a file to replace it.</p> : null}</div><div className="flex flex-wrap gap-3 sm:col-span-2"><Button type="submit" loading={saving} disabled={Boolean(newspaper && !canEdit) || (isNew && !canUpload)}>{isNew ? "Save draft" : "Save changes"}</Button>{canPublish ? <Button type="button" loading={saving} onClick={(event) => save(event as unknown as React.FormEvent, true)}>{newspaper?.status === "PUBLISHED" ? "Save & publish" : "Publish"}</Button> : null}{newspaper?.status === "PUBLISHED" && canPublish ? <Button type="button" onClick={unpublish} className="bg-white !text-ink ring-1 ring-line hover:bg-mist">Unpublish</Button> : null}{newspaper && canDelete ? <Button type="button" onClick={remove} className="bg-white !text-red-700 ring-1 ring-red-200 hover:bg-red-50">Delete</Button> : null}</div></Card></form></div></PermissionRoute></AdminRoute>;
}
