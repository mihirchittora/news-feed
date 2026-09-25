import { CategoryNav } from "@/components/public/CategoryNav";
import { BreakingNewsSection } from "@/components/public/BreakingNewsSection";
import { FeedList } from "@/components/public/FeedList";
import { AdBanner } from "@/components/public/AdBanner";

export default function HomePage() {
  return <main className="mx-auto max-w-6xl px-5 py-10 sm:py-14 lg:px-8"><div className="border-b border-line pb-8"><p className="text-xs font-bold uppercase tracking-[0.22em] text-coral">The daily signal</p><h1 className="mt-3 font-display text-5xl font-bold tracking-[-0.06em] text-ink sm:text-7xl">Latest stories.</h1><p className="mt-4 max-w-2xl text-base leading-7 text-slate">Clear reporting from the newsroom, organized by what matters to your community.</p><div className="mt-7"><CategoryNav /></div></div><div className="mt-8"><BreakingNewsSection /></div><section className="mt-8"><AdBanner placement="HOME_BANNER" /></section><section className="mt-8"><FeedList /></section></main>;
}
