import { AlertCircle, CheckCircle2 } from "lucide-react";

export function Alert({ type = "error", children }: { type?: "error" | "success"; children: React.ReactNode }) {
  const isSuccess = type === "success";
  return (
    <div
      className={`flex items-start gap-2 rounded-xl border px-3.5 py-3 text-sm ${
        isSuccess ? "border-emerald-200 bg-emerald-50 text-emerald-800" : "border-red-200 bg-red-50 text-red-800"
      }`}
      role={isSuccess ? "status" : "alert"}
    >
      {isSuccess ? <CheckCircle2 size={17} aria-hidden="true" /> : <AlertCircle size={17} aria-hidden="true" />}
      <span>{children}</span>
    </div>
  );
}
