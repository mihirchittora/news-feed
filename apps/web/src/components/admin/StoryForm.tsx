"use client";

import { RichTextEditor } from "@/components/admin/RichTextEditor";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { FormField } from "@/components/ui/FormField";
import { Input } from "@/components/ui/Input";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminStory, Category, StoryMedia, Tag } from "@/lib/types";
import { Eye, EyeOff, ImagePlus, Play, Trash2, Upload } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function safePreview(value: string) {
  return value
    .replace(/<script[\s\S]*?<\/script>/gi, "")
    .replace(/\son\w+\s*=\s*("[^"]*"|'[^']*')/gi, "");
}

export function StoryForm({ initialStory }: { initialStory?: AdminStory }) {
  const { token, hasPermission } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const isNew = !initialStory;
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [title, setTitle] = useState(initialStory?.title ?? "");
  const [summary, setSummary] = useState(initialStory?.summary ?? "");
  const [body, setBody] = useState(initialStory?.body ?? "");
  const [categoryId, setCategoryId] = useState(initialStory?.categoryId ?? "");
  const [tagIds, setTagIds] = useState<string[]>(initialStory?.tagIds ?? []);
  const [media, setMedia] = useState<StoryMedia[]>(initialStory?.media ?? []);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [preview, setPreview] = useState(() => searchParams.get("preview") === "1");

  const canCreate = hasPermission("STORY_CREATE");
  const canEdit = hasPermission("STORY_EDIT");
  const canPublish = hasPermission("STORY_PUBLISH");
  const canDelete = hasPermission("STORY_DELETE");
  const canSave = isNew ? canCreate : canEdit;

  useEffect(() => {
    if (!token) return;
    adminApi.categories(token).then(setCategories).catch(() => undefined);
    adminApi.tags(token).then(setTags).catch(() => undefined);
  }, [token]);

  function payload() {
    return { title, summary, body, categoryId: categoryId || undefined, tagIds, mediaIds: media.map((item) => item.id) };
  }

  async function save(andPublish = false) {
    if (!token || (!isNew && !canEdit) || (isNew && !canCreate)) return;
    if (!title.trim()) {
      setError("Title is required.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      const saved = isNew ? await adminApi.createStory(token, payload()) : await adminApi.updateStory(token, initialStory.id, payload());
      if (andPublish && canPublish) {
        const published = await adminApi.publishStory(token, saved.id);
        setNotice("Story published.");
        if (isNew) router.replace(`/admin/stories/${published.id}`);
      } else {
        setNotice(isNew ? "Draft saved." : "Changes saved.");
        if (isNew) router.replace(`/admin/stories/${saved.id}`);
      }
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not save this story.");
    } finally {
      setSaving(false);
    }
  }

  async function upload(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file || !token) return;
    setUploading(true);
    setError("");
    try {
      const uploaded = await adminApi.uploadMedia(token, file, initialStory?.id);
      setMedia((current) => [...current, uploaded]);
      setNotice("Media uploaded. Save the story to keep it attached.");
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not upload that file.");
    } finally {
      setUploading(false);
    }
  }

  function removeMedia(id: string) {
    setMedia((current) => current.filter((item) => item.id !== id));
  }

  function toggleTag(id: string) {
    setTagIds((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id]);
  }

  async function publish() {
    await save(true);
  }

  async function unpublish() {
    if (!token || !initialStory || !canPublish) return;
    try {
      await adminApi.unpublishStory(token, initialStory.id);
      setNotice("Story unpublished.");
      router.refresh();
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not unpublish this story.");
    }
  }

  async function removeStory() {
    if (!token || !initialStory || !canDelete || !window.confirm("Delete this story and its media?")) return;
    try {
      await adminApi.deleteStory(token, initialStory.id);
      router.replace("/admin/stories");
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "Could not delete this story.");
    }
  }

  return (
    <div className="max-w-5xl">
      {error ? <div className="mb-5"><Alert>{error}</Alert></div> : null}
      {notice ? <div className="mb-5"><Alert type="success">{notice}</Alert></div> : null}

      <div className="sticky top-4 z-20 mb-5 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-line bg-white/95 p-3 shadow-soft backdrop-blur">
        <div className="flex flex-wrap gap-2">
          {initialStory && canDelete ? <Button type="button" onClick={removeStory} className="bg-white !text-red-700 ring-1 ring-red-200 hover:bg-red-50">Delete</Button> : null}
          {initialStory && canPublish && initialStory.status === "PUBLISHED" ? <Button type="button" onClick={unpublish} className="bg-white !text-ink ring-1 ring-line hover:bg-mist">Unpublish</Button> : null}
        </div>
        <div className="flex flex-wrap gap-2">
          <Button type="button" onClick={() => setPreview((value) => !value)} className="bg-white !text-ink ring-1 ring-line hover:bg-mist">
            {preview ? <EyeOff size={16} /> : <Eye size={16} />} {preview ? "Edit" : "Preview"}
          </Button>
          {canSave ? <Button type="button" loading={saving} onClick={() => save(false)} className="bg-white !text-ink ring-1 ring-line hover:bg-mist">{isNew ? "Save draft" : "Save changes"}</Button> : null}
          {canPublish ? <Button type="button" loading={saving} onClick={publish}>{initialStory?.status === "PUBLISHED" ? "Save & publish" : "Publish"}</Button> : null}
        </div>
      </div>

      <Card className="p-5 sm:p-8">
        <div className="grid gap-6">
          <Input label="Title" value={title} onChange={(event) => setTitle(event.target.value)} maxLength={240} disabled={Boolean(initialStory && !canEdit)} />
          <FormField id="summary" label="Summary">
            <textarea id="summary" value={summary} onChange={(event) => setSummary(event.target.value)} maxLength={600} rows={3} disabled={Boolean(initialStory && !canEdit)} className="w-full rounded-xl border border-line bg-white px-4 py-3 text-[15px] text-ink outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="A concise description for the feed and search results" />
          </FormField>
          <FormField id="category" label="Category">
            <select id="category" value={categoryId} onChange={(event) => setCategoryId(event.target.value)} disabled={Boolean(initialStory && !canEdit)} className="min-h-12 w-full rounded-xl border border-line bg-white px-4 text-[15px] text-ink outline-none focus:border-coral focus:ring-4 focus:ring-coral/10">
              <option value="">Choose a category</option>
              {categories.filter((category) => category.status === "ACTIVE").map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
            </select>
          </FormField>
          <FormField id="tags" label="Tags">
            <div className="flex flex-wrap gap-2 rounded-xl border border-line p-3">
              {tags.length ? tags.map((tag) => <button key={tag.id} type="button" onClick={() => toggleTag(tag.id)} disabled={Boolean(initialStory && !canEdit)} className={`rounded-full px-3 py-1.5 text-xs font-semibold ${tagIds.includes(tag.id) ? "bg-ink text-white" : "bg-mist text-slate hover:bg-line"}`}>#{tag.name}</button>) : <span className="text-sm text-slate">Create tags in the Tags section first.</span>}
            </div>
          </FormField>
          <FormField id="body" label="Body"><RichTextEditor value={body} onChange={setBody} /></FormField>
          <div>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div><p className="text-sm font-bold text-ink">Media</p><p className="mt-1 text-xs text-slate">JPEG, PNG, WebP images up to 10 MB. MP4 video up to 50 MB.</p></div>
              <label className="inline-flex min-h-10 cursor-pointer items-center gap-2 rounded-xl border border-line px-4 text-sm font-semibold text-ink hover:border-ink"><Upload size={16} /> {uploading ? "Uploading…" : "Add media"}<input type="file" accept="image/jpeg,image/png,image/webp,video/mp4" className="sr-only" onChange={upload} disabled={uploading || Boolean(initialStory && !canEdit)} /></label>
            </div>
            {media.length ? <div className="mt-4 grid gap-3 sm:grid-cols-3">{media.map((item) => <div key={item.id} className="relative overflow-hidden rounded-2xl border border-line bg-mist">{item.type === "IMAGE" ? <img src={item.url} alt="Uploaded story media" className="h-36 w-full object-cover" /> : <div className="flex h-36 items-center justify-center text-ink"><Play size={26} fill="currentColor" /></div>}<button type="button" onClick={() => removeMedia(item.id)} disabled={Boolean(initialStory && !canEdit)} aria-label="Remove media" className="absolute right-2 top-2 grid h-8 w-8 place-items-center rounded-lg bg-white/90 text-red-700 shadow-sm"><Trash2 size={15} /></button></div>)}</div> : <div className="mt-4 rounded-2xl border border-dashed border-line px-5 py-8 text-center text-sm text-slate"><ImagePlus className="mx-auto mb-2 text-coral" size={22} />Stories can be text-only.</div>}
          </div>
        </div>
      </Card>

      {preview ? <Card className="mt-6 p-6 sm:p-10"><p className="text-xs font-bold uppercase tracking-[0.18em] text-coral">Authenticated preview</p><h2 className="mt-4 font-display text-4xl font-bold tracking-[-0.05em] text-ink">{title || "Untitled story"}</h2>{summary ? <p className="mt-4 text-lg text-slate">{summary}</p> : null}<div className="prose-news mt-7" dangerouslySetInnerHTML={{ __html: safePreview(body) }} /></Card> : null}
    </div>
  );
}
