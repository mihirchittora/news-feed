import { Card } from "@/components/ui/Card";

export function CategoryChart({ data }: { data: Array<{ category: string; published: number }> }) {
  const max = Math.max(...data.map((item) => item.published), 1);
  if (!data.length) return <Card className="p-6"><p className="text-sm text-slate">No stories published in this period.</p></Card>;
  return (
    <Card className="p-6">
      <div className="space-y-4" role="img" aria-label="Stories published by top-level category">
        {data.map((item) => (
          <div key={item.category}>
            <div className="mb-1.5 flex items-center justify-between gap-3 text-sm">
              <span className="truncate font-semibold text-ink">{item.category}</span>
              <span className="shrink-0 font-bold text-slate">{item.published}</span>
            </div>
            <div className="h-3 overflow-hidden rounded-full bg-mist" aria-hidden="true">
              <div className="h-full rounded-full bg-coral" style={{ width: `${Math.max((item.published / max) * 100, 4)}%` }} />
            </div>
          </div>
        ))}
      </div>
    </Card>
  );
}

export function PublishingTrendChart({ data }: { data: Array<{ date: string; published: number }> }) {
  const max = Math.max(...data.map((item) => item.published), 1);
  if (!data.length) return <Card className="p-6"><p className="text-sm text-slate">No publication data in this period.</p></Card>;
  return (
    <Card className="p-6">
      <div className="flex h-56 items-end gap-1.5 border-b border-line pb-0 sm:gap-2" role="img" aria-label="Stories published per calendar day">
        {data.map((item) => {
          const date = new Date(`${item.date}T12:00:00`);
          const label = date.toLocaleDateString(undefined, { month: "short", day: "numeric" });
          return (
            <div key={item.date} className="group flex h-full min-w-0 flex-1 flex-col items-center justify-end gap-2" title={`${label}: ${item.published} published`}>
              <span className="text-[10px] font-bold text-slate opacity-0 transition group-hover:opacity-100">{item.published}</span>
              <div className="w-full max-w-8 rounded-t-md bg-ink transition group-hover:bg-coral" style={{ height: `${item.published ? Math.max((item.published / max) * 78, 7) : 3}%` }} aria-hidden="true" />
              <span className="w-full truncate text-center text-[10px] text-slate">{data.length <= 10 || item === data[0] || item === data[data.length - 1] ? label : "·"}</span>
            </div>
          );
        })}
      </div>
      <p className="mt-3 text-xs text-slate">Counts use each calendar day in the configured application timezone.</p>
    </Card>
  );
}
