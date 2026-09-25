import type { HTMLAttributes } from "react";

export function Card({ className = "", ...props }: HTMLAttributes<HTMLElement>) {
  return <section className={`rounded-3xl border border-line bg-white shadow-soft ${className}`} {...props} />;
}
