"use client";

import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/EmptyState";
import { LoadingState } from "@/components/ui/LoadingState";
import { publicApi } from "@/lib/api/public";
import type { Advertisement, StorySummary } from "@/lib/types";
import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { FeedCard } from "@/components/public/FeedCard";
import { useAuth } from "@/components/auth/AuthProvider";
import { AdBanner } from "@/components/public/AdBanner";

const FEED_AD_INTERVAL = Math.max(1, Number(process.env.NEXT_PUBLIC_FEED_AD_INTERVAL ?? "5"));

export function FeedList({ filter, emptyTitle = "No stories yet", emptyDescription = "Published stories will appear here." }: { filter?: string; emptyTitle?: string; emptyDescription?: string }) {
  const { token } = useAuth();
  const [stories, setStories] = useState<StorySummary[]>([]); const [ads, setAds] = useState<Advertisement[]>([]); const [cursor, setCursor] = useState<string | null>(null); const [hasMore, setHasMore] = useState(false); const [loading, setLoading] = useState(true); const [loadingMore, setLoadingMore] = useState(false); const [error, setError] = useState("");
  const categorySlug = filter?.startsWith("category=") ? decodeURIComponent(filter.slice("category=".length)) : undefined;
  const adPlacement = categorySlug ? "CATEGORY_FEED" : "HOME_FEED";
  useEffect(() => { let active = true; setLoading(true); setError(""); Promise.all([publicApi.feed(filter ? `${filter}&limit=12` : "limit=12", token ?? undefined), publicApi.ads(adPlacement, categorySlug)]).then(([response, eligibleAds]) => { if (!active) return; setStories(response.items); setAds(eligibleAds); setCursor(response.nextCursor ?? null); setHasMore(response.hasMore); }).catch(() => active && setError("We could not load the latest stories. Please try again.")).finally(() => active && setLoading(false)); return () => { active = false; }; }, [adPlacement, categorySlug, filter, token]);
  async function loadMore() { if (!cursor || loadingMore) return; setLoadingMore(true); try { const response = await publicApi.feed(`${filter ? `${filter}&` : ""}limit=12&cursor=${encodeURIComponent(cursor)}`, token ?? undefined); setStories((current) => [...current, ...response.items]); setCursor(response.nextCursor ?? null); setHasMore(response.hasMore); } catch { setError("We could not load more stories."); } finally { setLoadingMore(false); } }
  if (loading) return <LoadingState label="Loading stories" />;
  if (error) return <Alert>{error}</Alert>;
  if (!stories.length) return <EmptyState title={emptyTitle} description={emptyDescription} />;
  const renderedItems: ReactNode[] = [];
  let insertedAd = false;
  stories.forEach((story, index) => {
    renderedItems.push(<FeedCard key={story.id} story={story} />);
    if (ads.length && (index + 1) % FEED_AD_INTERVAL === 0) {
      insertedAd = true;
      renderedItems.push(<AdBanner key={`ad-${story.id}`} advertisement={ads[(index / FEED_AD_INTERVAL) % ads.length]} placement={adPlacement} category={categorySlug} />);
    }
  });
  // Keep short feeds useful during development and low-volume publishing periods: if an
  // eligible campaign exists but the page has fewer than N stories, show one slot at the end.
  if (ads.length && !insertedAd) renderedItems.push(<AdBanner key="ad-short-feed" advertisement={ads[0]} placement={adPlacement} category={categorySlug} />);
  return <div className="space-y-5"><div className="space-y-5">{renderedItems}</div>{hasMore ? <div className="flex justify-center pt-3"><Button loading={loadingMore} onClick={loadMore}>Load more stories</Button></div> : null}</div>;
}
