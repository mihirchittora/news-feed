"use client";

import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/EmptyState";
import { LoadingState } from "@/components/ui/LoadingState";
import { publicApi } from "@/lib/api/public";
import type { StorySummary } from "@/lib/types";
import { useEffect, useState } from "react";
import { FeedCard } from "@/components/public/FeedCard";

export function FeedList({ filter, emptyTitle = "No stories yet", emptyDescription = "Published stories will appear here." }: { filter?: string; emptyTitle?: string; emptyDescription?: string }) {
  const [stories, setStories] = useState<StorySummary[]>([]); const [cursor, setCursor] = useState<string | null>(null); const [hasMore, setHasMore] = useState(false); const [loading, setLoading] = useState(true); const [loadingMore, setLoadingMore] = useState(false); const [error, setError] = useState("");
  useEffect(() => { let active = true; setLoading(true); setError(""); publicApi.feed(filter ? `${filter}&limit=12` : "limit=12").then((response) => { if (!active) return; setStories(response.items); setCursor(response.nextCursor ?? null); setHasMore(response.hasMore); }).catch(() => active && setError("We could not load the latest stories. Please try again.")).finally(() => active && setLoading(false)); return () => { active = false; }; }, [filter]);
  async function loadMore() { if (!cursor || loadingMore) return; setLoadingMore(true); try { const response = await publicApi.feed(`${filter ? `${filter}&` : ""}limit=12&cursor=${encodeURIComponent(cursor)}`); setStories((current) => [...current, ...response.items]); setCursor(response.nextCursor ?? null); setHasMore(response.hasMore); } catch { setError("We could not load more stories."); } finally { setLoadingMore(false); } }
  if (loading) return <LoadingState label="Loading stories" />;
  if (error) return <Alert>{error}</Alert>;
  if (!stories.length) return <EmptyState title={emptyTitle} description={emptyDescription} />;
  return <div className="space-y-5"><div className="space-y-5">{stories.map((story) => <FeedCard key={story.id} story={story} />)}</div>{hasMore ? <div className="flex justify-center pt-3"><Button loading={loadingMore} onClick={loadMore}>Load more stories</Button></div> : null}</div>;
}
