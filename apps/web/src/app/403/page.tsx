import Link from "next/link";

export default function ForbiddenPage() {
  return (
    <main className="grid min-h-[calc(100vh-73px)] place-items-center px-5 py-16">
      <div className="max-w-md text-center">
        <p className="text-xs font-bold uppercase tracking-[0.22em] text-coral">Access restricted</p>
        <p className="mt-5 font-display text-7xl font-bold tracking-[-0.08em] text-ink">403</p>
        <h1 className="mt-3 font-display text-3xl font-bold tracking-[-0.04em] text-ink">You don&apos;t have permission to access this page.</h1>
        <p className="mt-5 text-sm leading-6 text-slate">Your account is signed in, but this area is reserved for administrators.</p>
        <Link href="/" className="mt-8 inline-flex min-h-11 items-center rounded-xl bg-ink px-5 text-sm font-semibold text-white transition hover:bg-ink/90 focus:outline-none focus:ring-4 focus:ring-coral/20">Go home</Link>
      </div>
    </main>
  );
}
