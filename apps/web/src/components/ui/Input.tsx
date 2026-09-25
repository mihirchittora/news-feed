import type { InputHTMLAttributes } from "react";
import { FormField } from "@/components/ui/FormField";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

export function Input({ label, id, error, className = "", ...props }: InputProps) {
  const inputId = id ?? props.name ?? label.toLowerCase().replace(/\s+/g, "-");

  return (
    <FormField id={inputId} label={label} error={error}>
      <input
        id={inputId}
        className={`min-h-12 w-full rounded-xl border bg-white px-4 text-[15px] text-ink outline-none transition placeholder:text-slate/60 focus:border-coral focus:ring-4 focus:ring-coral/10 disabled:cursor-not-allowed disabled:bg-mist ${error ? "border-red-400" : "border-line"} ${className}`}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      />
    </FormField>
  );
}
