import type { ReactNode } from "react";

export function DashboardSection({ title, description, children, action }: { title: string; description?: string; children: ReactNode; action?: ReactNode }) {
  return (
    <section aria-labelledby={`dashboard-${title.toLowerCase().replace(/[^a-z0-9]+/g, "-")}`}>
      <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 id={`dashboard-${title.toLowerCase().replace(/[^a-z0-9]+/g, "-")}`} className="font-display text-3xl font-bold tracking-[-0.04em] text-ink">{title}</h2>
          {description ? <p className="mt-1 text-sm text-slate">{description}</p> : null}
        </div>
        {action}
      </div>
      {children}
    </section>
  );
}
