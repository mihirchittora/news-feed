import type { LucideIcon } from "lucide-react";
import Link from "next/link";

type DashboardMetricCardProps = {
  label: string;
  value: string | number;
  detail?: string;
  icon: LucideIcon;
  tone?: string;
  href?: string;
};

export function DashboardMetricCard({ label, value, detail, icon: Icon, tone = "bg-[#eaf1f4]", href }: DashboardMetricCardProps) {
  const content = (
    <div className={`rounded-2xl border border-line bg-white p-5 shadow-sm ${href ? "transition hover:-translate-y-0.5 hover:shadow-soft focus-within:ring-4 focus-within:ring-coral/15" : ""}`}>
      <div className="flex items-start justify-between gap-4">
        <div className={`grid h-10 w-10 shrink-0 place-items-center rounded-xl ${tone} text-ink`}><Icon size={18} aria-hidden="true" /></div>
        {href ? <span className="text-xs font-bold text-coral" aria-hidden="true">View →</span> : null}
      </div>
      <p className="mt-6 text-[11px] font-bold uppercase tracking-[0.17em] text-slate">{label}</p>
      <p className="mt-2 font-display text-3xl font-bold tracking-[-0.04em] text-ink">{value}</p>
      {detail ? <p className="mt-1 text-xs leading-5 text-slate">{detail}</p> : null}
    </div>
  );
  return href ? <Link href={href} aria-label={`${label}: ${value}`}>{content}</Link> : content;
}
