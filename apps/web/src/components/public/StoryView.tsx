"use client";

import { Alert } from "@/components/ui/Alert";
import { LoadingState } from "@/components/ui/LoadingState";
import { publicApi } from "@/lib/api/public";
import type { PublicStory } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";

export function StoryView({ slug }: { slug: string }) {
  const [story, setStory] = useState<PublicStory | null>(null); const [error, setError] = useState("");
  useEffect(() => { publicApi.story(slug).then(setStory).catch(() => setError("This story is unavailable or is no longer published.")); }, [slug]);
  if (error) return <main className="mx-auto max-w-3xl px-5 py-20"><Alert>{error}</Alert></main>;
  if (!story) return <LoadingState label="Loading story" />;
  return <main className="mx-auto max-w-4xl px-5 py-10 sm:py-16 lg:px-8"><article><div className="max-w-3xl"><Link href={story.category ? `/category/${story.category.slug}` : "/"} className="text-xs font-bold uppercase tracking-[0.2em] text-coral hover:underline">{story.category?.name ?? "News"}</Link><h1 className="mt-5 font-display text-5xl font-bold leading-[0.98] tracking-[-0.06em] text-ink sm:text-7xl">{story.title}</h1>{story.summary ? <p className="mt-6 text-xl leading-8 text-slate">{story.summary}</p> : null}<div className="mt-7 flex flex-wrap items-center gap-2 text-sm text-slate"><span>By {story.authorName}</span><span>•</span><time dateTime={story.publishedAt ?? undefined}>{story.publishedAt ? new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(story.publishedAt)) : ""}</time></div></div><div className="mt-10 space-y-5">{story.media.map((media) => media.type === "IMAGE" ? <img key={media.id} src={media.url} alt={story.title} className="max-h-[42rem] w-full rounded-3xl bg-mist object-contain" loading="lazy" /> : <video key={media.id} src={media.url} poster={media.thumbnailUrl ?? undefined} controls className="w-full rounded-3xl bg-ink" />)}</div><div className="prose-news mt-10 max-w-3xl" dangerouslySetInnerHTML={{ __html: story.body }} /><div className="mt-10 flex flex-wrap gap-2 border-t border-line pt-6">{story.tags.map((tag) => <Link key={tag.id} href={`/tag/${tag.slug}`} className="rounded-full bg-mist px-3 py-1.5 text-sm font-semibold text-slate hover:bg-ink hover:text-white">#{tag.name}</Link>)}</div></article></main>;
}
