import type { ReactNode } from "react";

export function EmptyState({ title, description, action }: { title: string; description: string; action?: ReactNode }) {
  return (
    <div className="rounded-2xl border border-dashed border-line bg-white/60 px-5 py-8 text-center sm:px-7">
      <p className="font-bold text-ink">{title}</p>
      <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-slate">{description}</p>
      {action ? <div className="mt-5">{action}</div> : null}
    </div>
  );
}
