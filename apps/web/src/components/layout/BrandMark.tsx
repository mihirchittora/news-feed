import Link from "next/link";

export function BrandMark({ compact = false, inverse = false }: { compact?: boolean; inverse?: boolean }) {
  return (
    <Link href="/" className="group inline-flex items-center gap-2.5" aria-label="News Platform home">
      <span className="grid h-9 w-9 place-items-center rounded-lg bg-coral text-sm font-black tracking-[-0.08em] text-white transition group-hover:bg-coral-dark">
        NF
      </span>
      {!compact ? (
        <span className={`font-display text-xl font-bold tracking-[-0.03em] ${inverse ? "text-white" : "text-ink"}`}>News Platform</span>
      ) : null}
    </Link>
  );
}
