"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { LoadingState } from "@/components/ui/LoadingState";
import { publicApi } from "@/lib/api/public";
import { ApiClientError, requestBlob } from "@/lib/api/client";
import type { Newspaper } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";

function formatDate(value: string) { return new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { day: "numeric", month: "long", year: "numeric" }); }

export function NewspaperReader({ id }: { id: string }) {
  const { user, token, isLoading: authLoading } = useAuth();
  const [edition, setEdition] = useState<Newspaper | null>(null);
  const [documentUrl, setDocumentUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => { publicApi.newspaper(id).then(setEdition).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "This edition is not available.")).finally(() => setLoading(false)); }, [id]);
  useEffect(() => {
    if (!token || !user) return;
    let active = true;
    requestBlob(`/api/v1/newspapers/${id}/document`, token).then((blob) => { if (active) setDocumentUrl(URL.createObjectURL(blob)); }).catch((reason) => { if (active) setError(reason instanceof ApiClientError ? reason.message : "Could not open this newspaper."); });
    return () => { active = false; setDocumentUrl((current) => { if (current) URL.revokeObjectURL(current); return null; }); };
  }, [id, token, user]);

  if (authLoading || loading) return <LoadingState label="Loading newspaper" />;
  if (error && !edition) return <main className="mx-auto max-w-3xl px-5 py-12"><Alert>{error}</Alert><Link href="/newspaper" className="mt-6 inline-flex text-sm font-bold text-ink underline decoration-coral decoration-2 underline-offset-4">Back to newspaper archive</Link></main>;
  if (!edition) return null;
  const returnPath = `/newspaper/${id}`;
  if (!user) return <main className="mx-auto max-w-2xl px-5 py-14"><Card className="p-7 text-center sm:p-12"><p className="text-xs font-bold uppercase tracking-[0.22em] text-coral">{formatDate(edition.editionDate)} · {edition.edition} Edition</p><h1 className="mt-4 font-display text-4xl font-bold tracking-[-0.05em] text-ink">This newspaper is available to registered users.</h1><p className="mt-4 leading-7 text-slate">Sign in or create a free account to read the full PDF edition.</p><div className="mt-7 flex flex-col justify-center gap-3 sm:flex-row"><Link href={`/login?next=${encodeURIComponent(returnPath)}`} className="inline-flex min-h-11 items-center justify-center rounded-xl bg-ink px-5 text-sm font-bold text-white">Sign In</Link><Link href={`/register?next=${encodeURIComponent(returnPath)}`} className="inline-flex min-h-11 items-center justify-center rounded-xl border border-line px-5 text-sm font-bold text-ink">Create Account</Link></div></Card></main>;
  return <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6 sm:py-10"><div className="flex flex-wrap items-end justify-between gap-4"><div><Link href="/newspaper" className="text-sm font-bold text-coral">← Newspaper archive</Link><p className="mt-4 text-xs font-bold uppercase tracking-[0.2em] text-slate">{formatDate(edition.editionDate)} · {edition.edition} Edition</p><h1 className="mt-2 font-display text-4xl font-bold tracking-[-0.05em] text-ink">{edition.title}</h1></div>{documentUrl ? <Button type="button" onClick={() => window.open(documentUrl, "_blank", "noopener,noreferrer")} className="bg-white !text-ink ring-1 ring-line hover:bg-mist">Open full screen</Button> : null}</div>{error ? <div className="mt-5"><Alert>{error}</Alert></div> : null}<div className="mt-7 overflow-hidden rounded-2xl border border-line bg-slate/10 shadow-soft">{documentUrl ? <iframe title={`${edition.title} ${edition.edition} edition`} src={documentUrl} className="h-[75vh] min-h-[34rem] w-full bg-white" /> : <LoadingState label="Opening PDF" />}</div><p className="mt-4 text-center text-xs text-slate">Use the PDF viewer controls to zoom and move between pages.</p></main>;
}
