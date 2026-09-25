"use client";

import { Eye, EyeOff } from "lucide-react";
import { useState, type InputHTMLAttributes } from "react";
import { FormField } from "@/components/ui/FormField";

type PasswordInputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

export function PasswordInput({ label, id, error, className = "", ...props }: PasswordInputProps) {
  const [visible, setVisible] = useState(false);
  const inputId = id ?? props.name ?? label.toLowerCase().replace(/\s+/g, "-");

  return (
    <FormField id={inputId} label={label} error={error}>
      <div className="relative">
        <input
          id={inputId}
          type={visible ? "text" : "password"}
          className={`min-h-12 w-full rounded-xl border bg-white px-4 pr-12 text-[15px] text-ink outline-none transition placeholder:text-slate/60 focus:border-coral focus:ring-4 focus:ring-coral/10 disabled:cursor-not-allowed disabled:bg-mist ${error ? "border-red-400" : "border-line"} ${className}`}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${inputId}-error` : undefined}
          {...props}
        />
        <button
          type="button"
          className="absolute right-2 top-1/2 grid h-9 w-9 -translate-y-1/2 place-items-center rounded-lg text-slate transition hover:bg-mist hover:text-ink focus:outline-none focus:ring-4 focus:ring-coral/15 disabled:pointer-events-none disabled:opacity-50"
          onClick={() => setVisible((current) => !current)}
          aria-label={visible ? `Hide ${label.toLowerCase()}` : `Show ${label.toLowerCase()}`}
          aria-pressed={visible}
          disabled={props.disabled}
        >
          {visible ? <EyeOff size={17} aria-hidden="true" /> : <Eye size={17} aria-hidden="true" />}
        </button>
      </div>
    </FormField>
  );
}
