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
  return <nav aria-label="News categories" className="flex flex-wrap gap-2 pb-1 text-sm font-semibold"><Link href="/" aria-current={latestActive ? "page" : undefined} className={`shrink-0 rounded-full border px-4 py-2 transition ${latestActive ? "border-ink bg-ink text-white" : "border-line bg-white text-slate hover:border-ink hover:text-ink"}`}>Latest</Link>{categories.map((category) => { const active = pathname === `/category/${category.slug}`; return <div key={category.id} className="flex flex-wrap items-center gap-2"><Link href={`/category/${category.slug}`} aria-current={active ? "page" : undefined} className={`shrink-0 rounded-full border px-4 py-2 transition ${active ? "border-ink bg-ink text-white" : "border-line bg-white text-slate hover:border-ink hover:text-ink"}`}>{category.name}</Link>{category.children?.map((child) => { const childActive = pathname === `/category/${category.slug}/${child.slug}`; return <Link key={child.id} href={`/category/${category.slug}/${child.slug}`} aria-current={childActive ? "page" : undefined} className={`shrink-0 rounded-full border border-dashed px-3 py-1.5 text-xs transition ${childActive ? "border-ink bg-ink text-white" : "border-line bg-white text-slate hover:border-ink hover:text-ink"}`}>{child.name}</Link>; })}</div>; })}</nav>;
}
