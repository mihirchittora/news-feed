import { ArrowRight, Bookmark, Clock3, Layers3, ShieldCheck } from "lucide-react";
import Link from "next/link";

const pillars = [
  { icon: Layers3, label: "Clear context", copy: "The foundation for a calmer, more useful news habit." },
  { icon: Bookmark, label: "Your place", copy: "An account that will keep your reading experience close." },
  { icon: Clock3, label: "Coming next", copy: "A front page built around the stories you want to follow." },
];

export default function HomePage() {
  return (
    <main>
      <section className="border-b border-line/80 bg-paper">
        <div className="mx-auto grid max-w-6xl gap-12 px-5 pb-20 pt-16 lg:grid-cols-[1.1fr_0.9fr] lg:px-8 lg:pb-28 lg:pt-24">
          <div className="max-w-3xl">
            <p className="text-xs font-bold uppercase tracking-[0.24em] text-coral">The front page is taking shape</p>
            <h1 className="mt-6 max-w-2xl font-display text-5xl font-bold leading-[0.96] tracking-[-0.06em] text-ink sm:text-7xl">News, with room to think.</h1>
            <p className="mt-7 max-w-xl text-lg leading-8 text-slate sm:text-xl">A considered digital news platform for following the stories that shape your world — without the noise.</p>
            <div className="mt-9 flex flex-wrap items-center gap-3">
              <Link href="/register" className="inline-flex min-h-12 items-center gap-2 rounded-xl bg-ink px-5 text-sm font-bold text-white transition hover:bg-ink/90">Create your account <ArrowRight size={16} aria-hidden="true" /></Link>
              <Link href="/login" className="inline-flex min-h-12 items-center rounded-xl border border-line px-5 text-sm font-bold text-ink transition hover:border-ink">Log in</Link>
            </div>
          </div>
          <div className="relative flex items-end justify-end lg:pt-8">
            <div className="w-full max-w-md rounded-3xl border border-line bg-white p-6 shadow-soft sm:p-8">
              <div className="flex items-center justify-between border-b border-line pb-5">
                <span className="text-xs font-bold uppercase tracking-[0.2em] text-coral">Platform note / 01</span>
                <span className="h-2 w-2 rounded-full bg-coral" aria-hidden="true" />
              </div>
              <p className="mt-7 font-display text-3xl font-bold leading-tight tracking-[-0.04em] text-ink">Start with the signal. Build from there.</p>
              <p className="mt-5 text-sm leading-6 text-slate">Account foundations are live today. Stories, editions, and richer ways to follow are next.</p>
              <div className="mt-8 flex items-center gap-3 border-t border-line pt-5 text-xs font-semibold text-slate"><ShieldCheck size={16} className="text-coral" aria-hidden="true" /> Built for a trusted reading experience</div>
            </div>
          </div>
        </div>
      </section>
      <section className="mx-auto max-w-6xl px-5 py-16 lg:px-8 lg:py-20">
        <div className="flex flex-col justify-between gap-4 border-b border-line pb-7 sm:flex-row sm:items-end">
          <div><p className="text-xs font-bold uppercase tracking-[0.2em] text-coral">Milestone one</p><h2 className="mt-3 font-display text-3xl font-bold tracking-[-0.04em] text-ink sm:text-4xl">A steady foundation.</h2></div>
          <p className="max-w-sm text-sm leading-6 text-slate">Create an account to see the platform’s first building blocks in action.</p>
        </div>
        <div className="grid gap-0 divide-y divide-line sm:grid-cols-3 sm:divide-x sm:divide-y-0">
          {pillars.map(({ icon: Icon, label, copy }, index) => <div className="py-7 sm:px-7 sm:first:pl-0 sm:last:pr-0" key={label}><span className="grid h-10 w-10 place-items-center rounded-xl bg-mist text-coral"><Icon size={18} aria-hidden="true" /></span><p className="mt-5 font-bold text-ink">0{index + 1} / {label}</p><p className="mt-2 text-sm leading-6 text-slate">{copy}</p></div>)}
        </div>
      </section>
      <footer className="border-t border-line bg-white"><div className="mx-auto flex max-w-6xl flex-col gap-2 px-5 py-7 text-xs text-slate sm:flex-row sm:items-center sm:justify-between lg:px-8"><span>News Platform · Milestone 1</span><span>Account foundations, thoughtfully made.</span></div></footer>
    </main>
  );
}
