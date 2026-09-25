import type { HTMLAttributes } from "react";

export function Badge({ className = "", ...props }: HTMLAttributes<HTMLSpanElement>) {
  return <span className={`inline-flex items-center rounded-full bg-mist px-2.5 py-1 text-xs font-semibold tracking-wide text-slate ${className}`} {...props} />;
}
