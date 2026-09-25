"use client";

import { publicApi } from "@/lib/api/public";
import type { Advertisement } from "@/lib/types";
import { ExternalLink } from "lucide-react";
import { useEffect, useState } from "react";

function placementLabel(value?: string) {
  return value === "HOME_BANNER" ? "Home Banner" : value === "CATEGORY_FEED" ? "Category Feed" : value === "NEWSPAPER" ? "Newspaper" : "Feed Ad";
}

function safeDestination(value?: string | null) {
  if (!value) return null;
  try {
    const url = new URL(value);
    return url.protocol === "http:" || url.protocol === "https:" ? value : null;
  } catch {
    return null;
  }
}

export function AdBanner({ advertisement, placement, category, className = "" }: { advertisement?: Advertisement; placement?: string; category?: string; className?: string }) {
  const [loaded, setLoaded] = useState<Advertisement | null | undefined>(advertisement);
  useEffect(() => {
    if (advertisement) { setLoaded(advertisement); return; }
    if (!placement) return;
    let active = true;
    publicApi.ads(placement, category).then((items) => { if (active) setLoaded(items[0] ?? null); }).catch(() => { if (active) setLoaded(null); });
    return () => { active = false; };
  }, [advertisement, category, placement]);

  if (!loaded) return null;
  const destination = safeDestination(loaded.destinationUrl);
  const content = <>
    <div className="flex items-center justify-between gap-3 text-[10px] font-bold uppercase tracking-[0.2em] text-slate"><span>Sponsored</span><span>{placementLabel(loaded.placementType ?? placement)}</span></div>
    <div className="mt-4 overflow-hidden rounded-2xl bg-mist">
      {loaded.mediaType === "VIDEO" ? <video src={loaded.mediaUrl} poster={loaded.thumbnailUrl ?? undefined} muted playsInline controls onError={(event) => { event.currentTarget.style.visibility = "hidden"; }} className="max-h-80 min-h-40 w-full object-cover" /> : <img src={loaded.mediaUrl} alt={`${loaded.advertiserName}: ${loaded.title}`} onError={(event) => { event.currentTarget.style.visibility = "hidden"; }} className="max-h-80 min-h-40 w-full object-cover" loading="lazy" />}
    </div>
    <div className="mt-4 flex flex-wrap items-end justify-between gap-4"><div><p className="text-sm font-bold text-ink">{loaded.advertiserName}</p><h2 className="mt-1 font-display text-2xl font-bold tracking-[-0.03em] text-ink">{loaded.title}</h2>{loaded.description ? <p className="mt-2 max-w-2xl text-sm leading-6 text-slate">{loaded.description}</p> : null}</div>{destination ? <span className="inline-flex min-h-10 items-center gap-2 rounded-xl bg-ink px-4 text-sm font-bold text-white">Learn more <ExternalLink size={14} aria-hidden="true" /></span> : null}</div>
  </>;

  return destination ? <a href={destination} target="_blank" rel="noopener noreferrer" className={`block rounded-3xl border border-line bg-white p-5 shadow-soft transition hover:-translate-y-0.5 hover:shadow-lg sm:p-7 ${className}`}>{content}</a> : <section className={`rounded-3xl border border-line bg-white p-5 shadow-soft sm:p-7 ${className}`}>{content}</section>;
}
