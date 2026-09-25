import type { HTMLAttributes } from "react";

export function PageContainer({ className = "", ...props }: HTMLAttributes<HTMLElement>) {
  return <main className={`mx-auto w-full max-w-6xl px-5 py-12 lg:px-8 lg:py-20 ${className}`} {...props} />;
}
