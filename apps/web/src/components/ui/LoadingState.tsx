import { Spinner } from "@/components/ui/Spinner";

export function LoadingState({ label = "Loading" }: { label?: string }) {
  return (
    <div className="flex min-h-[40vh] items-center justify-center px-6" role="status" aria-live="polite">
      <div className="flex items-center gap-3 text-sm text-slate">
        <Spinner size="sm" />
        {label}
      </div>
    </div>
  );
}
