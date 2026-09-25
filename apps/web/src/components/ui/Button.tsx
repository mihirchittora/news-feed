import type { ButtonHTMLAttributes } from "react";
import { Spinner } from "@/components/ui/Spinner";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  loading?: boolean;
};

export function Button({ className = "", children, loading = false, disabled, ...props }: ButtonProps) {
  return (
    <button
      className={`inline-flex min-h-11 items-center justify-center gap-2 rounded-xl bg-ink px-5 text-sm font-semibold text-white transition hover:bg-ink/90 focus:outline-none focus:ring-4 focus:ring-coral/20 disabled:cursor-not-allowed disabled:opacity-55 ${className}`}
      disabled={disabled || loading}
      aria-busy={loading}
      {...props}
    >
      {loading ? <Spinner size="sm" light /> : null}
      {children}
    </button>
  );
}
