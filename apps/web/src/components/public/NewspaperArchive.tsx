"use client";

import { Alert } from "@/components/ui/Alert";
import { Card } from "@/components/ui/Card";
import { LoadingState } from "@/components/ui/LoadingState";
import { publicApi } from "@/lib/api/public";
import { ApiClientError } from "@/lib/api/client";
import type { Newspaper } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";

function formatDate(value: string) { return new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { day: "numeric", month: "long", year: "numeric" }); }

export function NewspaperArchive() {
  const [items, setItems] = useState<Newspaper[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => { publicApi.newspapers().then((response) => setItems(response.items)).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load the newspaper archive.")).finally(() => setLoading(false)); }, []);

  return <main className="mx-auto max-w-6xl px-5 py-10 sm:py-14 lg:px-8"><div className="max-w-3xl"><p className="text-xs font-bold uppercase tracking-[0.22em] text-coral">The daily edition</p><h1 className="mt-3 font-display text-5xl font-bold tracking-[-0.06em] text-ink sm:text-7xl">Today&apos;s Newspaper</h1><p className="mt-5 text-lg leading-8 text-slate">Read the printed edition on your phone or desktop. The archive keeps recent editions close at hand.</p></div>{error ? <div className="mt-8"><Alert>{error}</Alert></div> : null}{loading ? <LoadingState label="Loading editions" /> : items.length === 0 ? <Card className="mt-10 p-10 text-center"><p className="font-semibold text-ink">No published editions yet.</p><p className="mt-2 text-sm text-slate">Check back soon for the next daily edition.</p></Card> : <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">{items.map((item) => <Card key={item.id} className="overflow-hidden"><div className="aspect-[3/4] bg-mist">{item.coverImageUrl ? <img src={item.coverImageUrl} alt={`${item.title}, ${item.edition} edition`} className="h-full w-full object-cover" /> : <div className="grid h-full place-items-center p-8 text-center"><span className="font-display text-2xl font-bold text-ink">{item.title}</span></div>}</div><div className="p-5"><p className="text-xs font-bold uppercase tracking-[0.16em] text-coral">{formatDate(item.editionDate)}</p><h2 className="mt-2 text-xl font-bold text-ink">{item.edition} Edition</h2><Link href={`/newspaper/${item.id}`} className="mt-5 inline-flex min-h-11 w-full items-center justify-center rounded-xl bg-ink px-4 text-sm font-bold text-white transition hover:bg-ink/90">Read Newspaper</Link></div></Card>)}</div>}</main>;
}
