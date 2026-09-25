"use client";

import { publicApi } from "@/lib/api/public";
import type { PublicCategory } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";

export function CategoryNav() {
  const [categories, setCategories] = useState<PublicCategory[]>([]);
  useEffect(() => { publicApi.categories().then(setCategories).catch(() => setCategories([])); }, []);
  return <nav aria-label="News categories" className="flex gap-2 overflow-x-auto pb-1 text-sm font-semibold"><Link href="/" className="shrink-0 rounded-full border border-ink bg-ink px-4 py-2 text-white">Latest</Link>{categories.map((category) => <Link key={category.id} href={`/category/${category.slug}`} className="shrink-0 rounded-full border border-line bg-white px-4 py-2 text-slate transition hover:border-ink hover:text-ink">{category.name}</Link>)}</nav>;
}
