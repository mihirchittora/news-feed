import Link from "next/link";

export default function NotFound() {
  return (
    <main className="mx-auto max-w-2xl px-5 py-20 text-center">
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-coral">404</p>
      <h1 className="mt-4 font-display text-4xl font-bold tracking-[-0.05em] text-ink">Page not found.</h1>
      <p className="mt-4 leading-7 text-slate">The page may have moved or is no longer public.</p>
      <Link href="/" className="mt-7 inline-flex min-h-11 items-center rounded-xl bg-ink px-5 text-sm font-bold text-white focus:outline-none focus:ring-4 focus:ring-coral/20">Return home</Link>
    </main>
  );
}
