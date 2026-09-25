"use client";

import { useEffect } from "react";

export default function Error({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    // Keep the client boundary intentionally quiet: exception details may contain sensitive data.
    console.error("Unexpected page error", { digest: "use client" });
  }, []);

  return (
    <main className="mx-auto max-w-2xl px-5 py-20 text-center">
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-coral">Something went wrong</p>
      <h1 className="mt-4 font-display text-4xl font-bold tracking-[-0.05em] text-ink">This page could not load.</h1>
      <p className="mt-4 leading-7 text-slate">Please try again. Your account and saved work are unchanged.</p>
      <button type="button" onClick={() => reset()} className="mt-7 min-h-11 rounded-xl bg-ink px-5 text-sm font-bold text-white focus:outline-none focus:ring-4 focus:ring-coral/20">Try again</button>
    </main>
  );
}
