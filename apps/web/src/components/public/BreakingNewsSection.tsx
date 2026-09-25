"use client";

import { BreakingBadge } from "@/components/public/BreakingBadge";
import { Alert } from "@/components/ui/Alert";
import { LoadingState } from "@/components/ui/LoadingState";
import { breakingNewsApi } from "@/lib/api/breaking-news";
import type { BreakingNewsItem } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";

function relativeTime(value: string) {
  const minutes = Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 60000));
  if (minutes < 1) return "Just now";
  if (minutes === 1) return "1 min ago";
  if (minutes < 60) return `${minutes} min ago`;
  const hours = Math.floor(minutes / 60);
  return `${hours} hr${hours === 1 ? "" : "s"} ago`;
}

export function BreakingNewsSection() {
  const [items, setItems] = useState<BreakingNewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    breakingNewsApi.listPublic()
      .then((response) => { if (active) setItems(response.items); })
      .catch(() => { if (active) setError("Breaking News is temporarily unavailable."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  if (loading) return <div className="rounded-2xl border border-line bg-white px-5 py-3"><LoadingState label="Checking for Breaking News" /></div>;
  if (error) return <Alert>{error}</Alert>;
  if (!items.length) return null;

  return (
    <section aria-labelledby="breaking-news-heading" className="rounded-3xl border border-red-200/80 bg-white p-5 shadow-sm sm:p-6">
      <div className="flex flex-wrap items-center gap-3 border-b border-red-100 pb-4">
        <BreakingBadge />
        <h2 id="breaking-news-heading" className="font-display text-2xl font-bold tracking-[-0.03em] text-ink">Breaking News</h2>
      </div>
      <div className="divide-y divide-line">
        {items.map((item) => (
          <Link key={item.id} href={`/story/${item.slug}`} className="group flex items-start justify-between gap-4 py-4 first:pt-5 last:pb-0 focus:outline-none focus:ring-4 focus:ring-coral/20">
            <div className="min-w-0"><p className="font-semibold leading-6 text-ink group-hover:text-coral">{item.title}</p><p className="mt-1 text-xs font-semibold uppercase tracking-[0.14em] text-slate">{item.category?.name ?? "News"}</p></div>
            <span className="shrink-0 pt-1 text-xs font-semibold text-slate">{relativeTime(item.breakingStartedAt)}</span>
          </Link>
        ))}
      </div>
    </section>
  );
}
