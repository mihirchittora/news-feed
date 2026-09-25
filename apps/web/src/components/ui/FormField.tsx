import type { ReactNode } from "react";

type FormFieldProps = {
  id: string;
  label: string;
  error?: string;
  children: ReactNode;
};

export function FormField({ id, label, error, children }: FormFieldProps) {
  return (
    <div className="space-y-2">
      <label className="block text-sm font-semibold text-ink" htmlFor={id}>
        {label}
      </label>
      {children}
      {error ? (
        <p id={`${id}-error`} className="text-xs leading-5 text-red-700" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  );
}
