"use client";

import { publicApi } from "@/lib/api/public";
import type { PublicCategory } from "@/lib/types";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

export function CategoryNav() {
  const [categories, setCategories] = useState<PublicCategory[]>([]);
  const pathname = usePathname();
  useEffect(() => { publicApi.categories().then(setCategories).catch(() => setCategories([])); }, []);
  const latestActive = pathname === "/";
  return <nav aria-label="News categories" className="flex gap-2 overflow-x-auto pb-1 text-sm font-semibold"><Link href="/" aria-current={latestActive ? "page" : undefined} className={`shrink-0 rounded-full border px-4 py-2 transition ${latestActive ? "border-ink bg-ink text-white" : "border-line bg-white text-slate hover:border-ink hover:text-ink"}`}>Latest</Link>{categories.map((category) => { const active = pathname === `/category/${category.slug}`; return <Link key={category.id} href={`/category/${category.slug}`} aria-current={active ? "page" : undefined} className={`shrink-0 rounded-full border px-4 py-2 transition ${active ? "border-ink bg-ink text-white" : "border-line bg-white text-slate hover:border-ink hover:text-ink"}`}>{category.name}</Link>; })}</nav>;
}
