export function BreakingBadge({ compact = false }: { compact?: boolean }) {
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border border-red-200 bg-red-50 px-2.5 py-1 text-[10px] font-extrabold uppercase tracking-[0.14em] text-red-700 ${compact ? "text-[9px]" : ""}`}>
      <span aria-hidden="true">🔴</span>
      <span>Breaking</span>
    </span>
  );
}
