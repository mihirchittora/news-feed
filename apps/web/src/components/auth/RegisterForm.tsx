"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { PasswordInput } from "@/components/ui/PasswordInput";
import { ApiClientError } from "@/lib/api/client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

type FormErrors = Partial<Record<"name" | "email" | "password" | "confirmPassword", string>>;

export function RegisterForm({ nextPath }: { nextPath?: string }) {
  const { register } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState({ name: "", email: "", password: "", confirmPassword: "" });
  const [errors, setErrors] = useState<FormErrors>({});
  const [serverError, setServerError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function update(field: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function validate() {
    const nextErrors: FormErrors = {};
    if (!form.name.trim()) nextErrors.name = "Name is required";
    if (!form.email.trim()) nextErrors.email = "Email is required";
    else if (!/^\S+@\S+\.\S+$/.test(form.email)) nextErrors.email = "Enter a valid email address";
    if (!form.password) nextErrors.password = "Password is required";
    else if (form.password.length < 8) nextErrors.password = "Password must be at least 8 characters";
    if (!form.confirmPassword) nextErrors.confirmPassword = "Please confirm your password";
    else if (form.confirmPassword !== form.password) nextErrors.confirmPassword = "Passwords do not match";
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setServerError("");
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      await register({ name: form.name.trim(), email: form.email.trim(), password: form.password });
      router.replace(`/login?registered=1${nextPath && nextPath.startsWith("/") ? `&next=${encodeURIComponent(nextPath)}` : ""}`);
    } catch (error) {
      const apiError = error instanceof ApiClientError ? error : null;
      if (apiError?.errors) {
        setErrors((current) => ({ ...current, ...apiError.errors }));
      }
      setServerError(apiError?.message ?? "Unable to create your account right now");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-8">
        <h2 className="font-display text-3xl font-bold tracking-[-0.03em] text-ink">Create account</h2>
        <p className="mt-2 text-sm leading-6 text-slate">Join the platform now; your personalized feed comes next.</p>
      </div>
      <form className="space-y-4" onSubmit={handleSubmit} noValidate>
        {serverError ? <Alert>{serverError}</Alert> : null}
        <Input label="Name" name="name" type="text" autoComplete="name" value={form.name} onChange={(event) => update("name", event.target.value)} error={errors.name} disabled={isSubmitting} />
        <Input label="Email" name="email" type="email" autoComplete="email" value={form.email} onChange={(event) => update("email", event.target.value)} error={errors.email} disabled={isSubmitting} />
        <PasswordInput label="Password" name="password" autoComplete="new-password" value={form.password} onChange={(event) => update("password", event.target.value)} error={errors.password} disabled={isSubmitting} />
        <PasswordInput label="Confirm password" name="confirmPassword" autoComplete="new-password" value={form.confirmPassword} onChange={(event) => update("confirmPassword", event.target.value)} error={errors.confirmPassword} disabled={isSubmitting} />
        <Button className="mt-2 w-full" type="submit" loading={isSubmitting}>{isSubmitting ? "Creating account…" : "Create account"}</Button>
      </form>
      <p className="mt-7 text-center text-sm text-slate">Already registered? <Link href="/login" className="font-bold text-ink underline decoration-coral decoration-2 underline-offset-4">Log in</Link></p>
    </div>
  );
}
