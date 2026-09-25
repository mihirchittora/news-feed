"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { AdPreview } from "@/components/admin/AdPreview";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi, type AdvertisementPayload } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import { publicApi } from "@/lib/api/public";
import type { AdminAdvertisement, Category, PublicCategory, StoryMedia } from "@/lib/types";
import { ArrowLeft, ImagePlus, Pause, Play, Save, Trash2, Upload } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import type { ChangeEvent } from "react";

const placements = [
  ["HOME_BANNER", "Home Banner"],
  ["HOME_FEED", "Home Feed"],
  ["CATEGORY_FEED", "Category Feed"],
  ["NEWSPAPER", "Newspaper"],
] as const;

function localDateTime(value?: string | null) {
  if (!value) return "";
  const date = new Date(value);
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function toInstant(value: string) { return new Date(value).toISOString(); }
function displayStatus(value?: string) { return value ? value[0] + value.slice(1).toLowerCase() : "Draft"; }

function flattenCategories(items: PublicCategory[], parentId?: string): Category[] {
  return items.flatMap((item, index) => [
    { id: item.id, name: item.name, slug: item.slug, description: item.description, status: "ACTIVE", displayOrder: index, parentId },
    ...flattenCategories(item.children ?? [], item.id),
  ]);
}

type AdvertisementFormProps = { initialAdvertisement?: AdminAdvertisement };

export function AdvertisementForm({ initialAdvertisement }: AdvertisementFormProps) {
  const { token, hasPermission } = useAuth();
  const router = useRouter();
  const [title, setTitle] = useState(initialAdvertisement?.title ?? "");
  const [advertiserName, setAdvertiserName] = useState(initialAdvertisement?.advertiserName ?? "");
  const [description, setDescription] = useState(initialAdvertisement?.description ?? "");
  const [destinationUrl, setDestinationUrl] = useState(initialAdvertisement?.destinationUrl ?? "");
  const [startAt, setStartAt] = useState(localDateTime(initialAdvertisement?.startAt));
  const [endAt, setEndAt] = useState(localDateTime(initialAdvertisement?.endAt));
  const [placement, setPlacement] = useState(initialAdvertisement?.placements[0]?.type ?? "HOME_BANNER");
  const [categoryIds, setCategoryIds] = useState<string[]>(initialAdvertisement?.placements.flatMap((item) => item.categoryId ? [item.categoryId] : []) ?? []);
  const [categories, setCategories] = useState<Category[]>([]);
  const [uploadedMedia, setUploadedMedia] = useState<StoryMedia | null>(null);
  const [uploading, setUploading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const canEdit = !initialAdvertisement || hasPermission("AD_EDIT");
  const canPublish = hasPermission("AD_PUBLISH");
  const canPause = hasPermission("AD_PAUSE");
  const canDelete = hasPermission("AD_DELETE");
  const categoryRoots = useMemo(() => categories.filter((category) => !category.parentId), [categories]);
  const categoryChildren = useMemo(() => categories.reduce<Record<string, Category[]>>((map, category) => {
    if (category.parentId) (map[category.parentId] ??= []).push(category);
    return map;
  }, {}), [categories]);
  const preview = uploadedMedia
    ? { advertiserName, title, description, mediaType: uploadedMedia.type, mediaUrl: uploadedMedia.url, thumbnailUrl: uploadedMedia.thumbnailUrl, destinationUrl }
    : initialAdvertisement ?? { advertiserName, title, description, destinationUrl };

  useEffect(() => { publicApi.categories().then((items) => setCategories(flattenCategories(items))).catch(() => undefined); }, []);

  function toggleCategory(id: string) {
    setCategoryIds((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id]);
  }

  async function upload(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file || !token || !canEdit) return;
    setUploading(true); setError("");
    try {
      setUploadedMedia(await adminApi.uploadMedia(token, file));
      setNotice("Creative uploaded. Save the advertisement to attach it to this campaign.");
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not upload the creative.");
    } finally { setUploading(false); }
  }

  function payload(): AdvertisementPayload {
    return {
      title,
      advertiserName,
      description,
      destinationUrl,
      startAt: toInstant(startAt),
      endAt: toInstant(endAt),
      placement: { type: placement, categoryIds: placement === "CATEGORY_FEED" ? categoryIds : [] },
      ...(uploadedMedia ? { mediaId: uploadedMedia.id } : {}),
    };
  }

  async function save(event: { preventDefault: () => void }, publish = false) {
    event.preventDefault();
    if (!token || !canEdit) return;
    setError(""); setNotice("");
    if (!startAt || !endAt || new Date(startAt) >= new Date(endAt)) { setError("End time must be after start time."); return; }
    if (placement === "CATEGORY_FEED" && categoryIds.length === 0) { setError("Select at least one category for a category feed advertisement."); return; }
    if (!initialAdvertisement && !uploadedMedia) { setError("Upload an image or MP4 video before saving the advertisement."); return; }
    setSaving(true);
    try {
      const saved = initialAdvertisement
        ? await adminApi.updateAdvertisement(token, initialAdvertisement.id, payload())
        : await adminApi.createAdvertisement(token, payload());
      if (publish && canPublish) {
        const published = await adminApi.publishAdvertisement(token, saved.id);
        setNotice(published.status === "SCHEDULED" ? "Advertisement scheduled." : "Advertisement published and active.");
      } else {
        setNotice(initialAdvertisement ? "Advertisement changes saved." : "Advertisement saved as a draft.");
      }
      if (!initialAdvertisement) router.replace(`/admin/ads/${saved.id}`);
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not save the advertisement.");
    } finally { setSaving(false); }
  }

  async function changeStatus(action: "pause" | "resume") {
    if (!token || !initialAdvertisement || !canPause) return;
    const message = action === "pause" ? "Pause advertisement? It will stop appearing immediately." : "Resume advertisement? The campaign will become eligible according to its schedule.";
    if (!window.confirm(message)) return;
    setError("");
    try {
      const next = action === "pause" ? await adminApi.pauseAdvertisement(token, initialAdvertisement.id) : await adminApi.resumeAdvertisement(token, initialAdvertisement.id);
      setNotice(`Advertisement ${action === "pause" ? "paused" : "resumed"}.`);
      window.location.assign(`/admin/ads/${next.id}`);
    } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : `Could not ${action} the advertisement.`); }
  }

  async function remove() {
    if (!token || !initialAdvertisement || !canDelete || !window.confirm("Delete advertisement? This will remove it from administration.")) return;
    try { await adminApi.deleteAdvertisement(token, initialAdvertisement.id); router.replace("/admin/ads"); }
    catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not delete the advertisement."); }
  }

  const mediaUrl = uploadedMedia?.url ?? initialAdvertisement?.mediaUrl;
  const mediaType = uploadedMedia?.type ?? initialAdvertisement?.mediaType;
  const statusMessage = initialAdvertisement?.status === "ACTIVE"
    ? `Active until ${new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(initialAdvertisement.endAt))}.`
    : initialAdvertisement?.status === "SCHEDULED"
      ? `Starts ${new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(initialAdvertisement.startAt))}.`
      : initialAdvertisement?.status === "EXPIRED"
        ? `Expired ${new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(initialAdvertisement.endAt))}.`
        : "Drafts are not visible publicly.";

  return (
    <AdminShell>
      <div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end">
        <div><Link href="/admin/ads" className="inline-flex items-center gap-2 text-sm font-bold text-coral"><ArrowLeft size={15} /> Advertisements</Link><PageHeader eyebrow={initialAdvertisement ? `Advertising / ${displayStatus(initialAdvertisement.status)}` : "Advertising"} title={initialAdvertisement ? initialAdvertisement.title : "Create advertisement"} description={initialAdvertisement ? "Manage the campaign schedule, creative, placements, and publication state." : "Create a clearly labeled campaign for the public platform."} /></div>
        {initialAdvertisement ? <Badge className={initialAdvertisement.status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : initialAdvertisement.status === "PAUSED" ? "bg-amber-50 text-amber-700" : initialAdvertisement.status === "EXPIRED" ? "bg-slate-100 text-slate" : "bg-mist text-slate"}>{displayStatus(initialAdvertisement.status)}</Badge> : null}
      </div>
      {error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}
      {notice ? <div className="mt-6"><Alert type="success">{notice}</Alert></div> : null}
      <form onSubmit={(event) => void save(event)} className="mt-8 grid gap-6 xl:grid-cols-[minmax(0,1fr)_22rem]">
        <div className="space-y-6">
          <Card className="p-5 sm:p-8"><div className="grid gap-5"><Input label="Advertiser" value={advertiserName} onChange={(event) => setAdvertiserName(event.target.value)} maxLength={180} disabled={!canEdit} required /><Input label="Campaign title" value={title} onChange={(event) => setTitle(event.target.value)} maxLength={240} disabled={!canEdit} required /><label className="grid gap-2 text-sm font-semibold text-ink">Description<textarea value={description} onChange={(event) => setDescription(event.target.value)} maxLength={5000} rows={4} disabled={!canEdit} className="w-full rounded-xl border border-line bg-white px-4 py-3 text-[15px] text-ink outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="A short, clear description of the offer." /></label><Input label="Destination URL (optional)" value={destinationUrl} onChange={(event) => setDestinationUrl(event.target.value)} type="url" disabled={!canEdit} placeholder="https://example.com" /></div></Card>
          <Card className="p-5 sm:p-8"><div className="flex items-start justify-between gap-4"><div><h2 className="text-lg font-bold text-ink">Creative</h2><p className="mt-1 text-sm text-slate">JPEG, PNG, WebP images or MP4 video. Existing media validation is applied.</p></div>{canEdit ? <label className="inline-flex min-h-10 cursor-pointer items-center gap-2 rounded-xl border border-line px-4 text-sm font-semibold text-ink hover:border-ink"><Upload size={16} /> {uploading ? "Uploading…" : "Upload"}<input type="file" accept="image/jpeg,image/png,image/webp,video/mp4" className="sr-only" onChange={upload} disabled={uploading} /></label> : null}</div>{mediaUrl ? <div className="mt-5 overflow-hidden rounded-2xl bg-mist">{mediaType === "VIDEO" ? <video src={mediaUrl} poster={uploadedMedia?.thumbnailUrl ?? initialAdvertisement?.thumbnailUrl ?? undefined} controls muted playsInline className="max-h-96 min-h-48 w-full object-cover" /> : <img src={mediaUrl} alt="Advertisement creative" className="max-h-96 min-h-48 w-full object-cover" />}</div> : <div className="mt-5 grid min-h-48 place-items-center rounded-2xl border border-dashed border-line bg-mist/50 p-8 text-center text-sm text-slate"><ImagePlus className="mb-2 text-coral" size={26} /><span>Upload a creative to continue.</span></div>}</Card>
          <Card className="p-5 sm:p-8"><h2 className="text-lg font-bold text-ink">Schedule</h2><p className="mt-1 text-sm text-slate">The campaign becomes eligible only between these UTC instants.</p><div className="mt-5 grid gap-5 sm:grid-cols-2"><label className="grid gap-2 text-sm font-semibold text-ink">Start<input type="datetime-local" value={startAt} onChange={(event) => setStartAt(event.target.value)} disabled={!canEdit} required className="min-h-12 rounded-xl border border-line bg-white px-3 text-sm outline-none focus:border-coral" /></label><label className="grid gap-2 text-sm font-semibold text-ink">End<input type="datetime-local" value={endAt} onChange={(event) => setEndAt(event.target.value)} disabled={!canEdit} required className="min-h-12 rounded-xl border border-line bg-white px-3 text-sm outline-none focus:border-coral" /></label></div></Card>
          <Card className="p-5 sm:p-8"><h2 className="text-lg font-bold text-ink">Placement</h2><p className="mt-1 text-sm text-slate">Choose an application-defined placement. A parent category applies to its child feeds.</p><div className="mt-5 grid gap-3 sm:grid-cols-2">{placements.map(([value, label]) => <label key={value} className={`flex cursor-pointer items-center gap-3 rounded-2xl border px-4 py-4 text-sm font-semibold ${placement === value ? "border-coral bg-coral/5 text-ink" : "border-line text-slate"}`}><input type="radio" name="placement" value={value} checked={placement === value} onChange={(event) => { setPlacement(event.target.value); if (event.target.value !== "CATEGORY_FEED") setCategoryIds([]); }} disabled={!canEdit} className="accent-coral" />{label}</label>)}</div>{placement === "CATEGORY_FEED" ? <div className="mt-6 border-t border-line pt-5"><p className="text-sm font-bold text-ink">Select categories</p><div className="mt-3 grid gap-3 sm:grid-cols-2">{categoryRoots.map((root) => <div key={root.id} className="rounded-2xl border border-line p-4"><label className="flex items-center gap-3 text-sm font-bold text-ink"><input type="checkbox" checked={categoryIds.includes(root.id)} onChange={() => toggleCategory(root.id)} disabled={!canEdit} className="accent-coral" />{root.name}</label>{(categoryChildren[root.id] ?? []).length ? <div className="mt-3 space-y-2 border-l border-line pl-5">{categoryChildren[root.id].map((child) => <label key={child.id} className="flex items-center gap-3 text-sm text-slate"><input type="checkbox" checked={categoryIds.includes(child.id)} onChange={() => toggleCategory(child.id)} disabled={!canEdit} className="accent-coral" />{child.name}</label>)}</div> : null}</div>)}</div></div> : null}</Card>
        </div>
        <div className="space-y-6"><AdPreview ad={preview} placement={placement} /><Card className="p-5"><p className="text-xs font-bold uppercase tracking-[0.18em] text-coral">Campaign state</p><p className="mt-3 text-sm leading-6 text-slate">{statusMessage}</p></Card><div className="flex flex-wrap gap-3">{canEdit ? <Button type="submit" loading={saving}><Save size={16} /> {initialAdvertisement ? "Save changes" : "Save draft"}</Button> : null}{canEdit && canPublish ? <Button type="button" loading={saving} onClick={(event) => void save(event, true)}><Play size={16} /> {initialAdvertisement?.status === "ACTIVE" ? "Save & publish" : "Publish"}</Button> : null}{initialAdvertisement?.status === "ACTIVE" || initialAdvertisement?.status === "SCHEDULED" ? canPause ? <Button type="button" onClick={() => void changeStatus("pause")} className="bg-white !text-ink ring-1 ring-line hover:bg-mist"><Pause size={16} /> Pause</Button> : null : initialAdvertisement?.status === "PAUSED" && canPause ? <Button type="button" onClick={() => void changeStatus("resume")}><Play size={16} /> Resume</Button> : null}{initialAdvertisement && canDelete ? <Button type="button" onClick={() => void remove()} className="bg-white !text-red-700 ring-1 ring-red-200 hover:bg-red-50"><Trash2 size={16} /> Delete</Button> : null}</div></div>
      </form>
    </AdminShell>
  );
}
