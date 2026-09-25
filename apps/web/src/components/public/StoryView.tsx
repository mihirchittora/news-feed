"use client";

import { Alert } from "@/components/ui/Alert";
import { LoadingState } from "@/components/ui/LoadingState";
import { CommentsSection } from "@/components/public/CommentsSection";
import { BreakingBadge } from "@/components/public/BreakingBadge";
import { LikeButton } from "@/components/public/LikeButton";
import { useAuth } from "@/components/auth/AuthProvider";
import { publicApi } from "@/lib/api/public";
import type { PublicStory } from "@/lib/types";
import { MessageCircle } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";

export function StoryView({ slug }: { slug: string }) {
  const { token } = useAuth();
  const [story, setStory] = useState<PublicStory | null>(null); const [error, setError] = useState("");
  useEffect(() => { setStory(null); setError(""); publicApi.story(slug, token ?? undefined).then(setStory).catch(() => setError("This story is unavailable or is no longer published.")); }, [slug, token]);
  if (error) return <main className="mx-auto max-w-3xl px-5 py-20"><Alert>{error}</Alert></main>;
  if (!story) return <LoadingState label="Loading story" />;
  return <main className="mx-auto max-w-4xl px-5 py-10 sm:py-16 lg:px-8"><article><div className="max-w-3xl">{story.isBreaking ? <BreakingBadge /> : null}<Link href={story.category ? `/category/${story.category.slug}` : "/"} className="mt-4 block text-xs font-bold uppercase tracking-[0.2em] text-coral hover:underline">{story.category?.name ?? "News"}</Link><h1 className="mt-5 font-display text-5xl font-bold leading-[0.98] tracking-[-0.06em] text-ink sm:text-7xl">{story.title}</h1>{story.summary ? <p className="mt-6 text-xl leading-8 text-slate">{story.summary}</p> : null}<div className="mt-7 flex flex-wrap items-center gap-2 text-sm text-slate"><span>By {story.authorName}</span><span>•</span><time dateTime={story.publishedAt ?? undefined}>{story.publishedAt ? new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(story.publishedAt)) : ""}</time></div></div><div className="mt-10 space-y-5">{story.media.map((media) => media.type === "IMAGE" ? <img key={media.id} src={media.url} alt={story.title} onError={(event) => { event.currentTarget.style.visibility = "hidden"; }} className="max-h-[42rem] w-full rounded-3xl bg-mist object-contain" loading="lazy" /> : <video key={media.id} src={media.url} poster={media.thumbnailUrl ?? undefined} controls onError={(event) => { event.currentTarget.style.visibility = "hidden"; }} className="w-full rounded-3xl bg-ink" />)}</div><div className="prose-news mt-10 max-w-3xl" dangerouslySetInnerHTML={{ __html: story.body }} /><div className="mt-8 flex flex-wrap items-center gap-2 border-y border-line py-3"><LikeButton storyId={story.id} initialLikeCount={story.likeCount} initialLiked={story.likedByCurrentUser} loginHref={`/login?next=${encodeURIComponent(`/story/${story.slug}`)}`} /><Link href="#comments" className="inline-flex min-h-10 items-center gap-2 rounded-xl px-3 text-sm font-bold text-slate transition hover:bg-mist hover:text-ink"><MessageCircle size={17} aria-hidden="true" /><span>{story.commentCount}</span><span>Comments</span></Link></div><div className="mt-8 flex flex-wrap gap-2">{story.tags.map((tag) => <Link key={tag.id} href={`/tag/${tag.slug}`} className="rounded-full bg-mist px-3 py-1.5 text-sm font-semibold text-slate hover:bg-ink hover:text-white">#{tag.name}</Link>)}</div></article><CommentsSection storyId={story.id} initialCommentCount={story.commentCount} loginHref={`/login?next=${encodeURIComponent(`/story/${story.slug}#comments`)}`} onCommentCountChange={(count) => setStory((current) => current ? { ...current, commentCount: count } : current)} /></main>;
}
