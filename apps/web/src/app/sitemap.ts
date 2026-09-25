import type { MetadataRoute } from "next";

const apiBase = (process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080").replace(/\/$/, "");
const siteUrl = (process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000").replace(/\/$/, "");

async function readJson<T>(path: string): Promise<T | null> {
  try {
    const response = await fetch(`${apiBase}${path}`, { next: { revalidate: 300 } });
    return response.ok ? await response.json() as T : null;
  } catch {
    return null;
  }
}

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const [categories, feed, newspapers] = await Promise.all([
    readJson<Array<{ slug: string; children?: Array<{ slug: string }> }>>("/api/v1/categories"),
    readJson<{ items: Array<{ slug: string; publishedAt?: string | null }> }>("/api/v1/feed?limit=50"),
    readJson<{ items: Array<{ id: string; publishedAt?: string | null }> }>("/api/v1/newspapers"),
  ]);
  const categorySlugs = (categories ?? []).flatMap((category) => [category.slug, ...(category.children ?? []).map((child) => child.slug)]);
  return [
    { url: siteUrl, changeFrequency: "hourly", priority: 1 },
    { url: `${siteUrl}/newspaper`, changeFrequency: "daily", priority: 0.7 },
    ...categorySlugs.map((slug) => ({ url: `${siteUrl}/category/${slug}`, changeFrequency: "hourly" as const, priority: 0.7 })),
    ...(feed?.items ?? []).map((story) => ({ url: `${siteUrl}/story/${story.slug}`, lastModified: story.publishedAt ?? undefined, changeFrequency: "daily" as const, priority: 0.8 })),
    ...(newspapers?.items ?? []).map((edition) => ({ url: `${siteUrl}/newspaper/${edition.id}`, lastModified: edition.publishedAt ?? undefined, changeFrequency: "daily" as const, priority: 0.6 })),
  ];
}
