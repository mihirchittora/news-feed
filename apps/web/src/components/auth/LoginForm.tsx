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

type FormErrors = Partial<Record<"email" | "password", string>>;

export function LoginForm({ registrationSuccess = false, nextPath }: { registrationSuccess?: boolean; nextPath?: string }) {
  const { login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<FormErrors>({});
  const [serverError, setServerError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function validate() {
    const nextErrors: FormErrors = {};
    if (!email.trim()) nextErrors.email = "Email is required";
    else if (!/^\S+@\S+\.\S+$/.test(email)) nextErrors.email = "Enter a valid email address";
    if (!password) nextErrors.password = "Password is required";
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setServerError("");
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      const user = await login({ email: email.trim(), password });
      router.replace(nextPath && nextPath.startsWith("/") ? nextPath : user.role === "ADMIN" ? "/admin/dashboard" : "/");
    } catch (error) {
      setServerError(error instanceof ApiClientError ? error.message : "Unable to sign in. Please check your email and password.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-8">
        <h2 className="font-display text-3xl font-bold tracking-[-0.03em] text-ink">Welcome back</h2>
        <p className="mt-2 text-sm leading-6 text-slate">Sign in to keep your place in the story.</p>
      </div>
      <form className="space-y-5" onSubmit={handleSubmit} noValidate>
        {registrationSuccess ? <Alert type="success">Account created successfully. Please sign in to continue.</Alert> : null}
        {serverError ? <Alert>{serverError}</Alert> : null}
        <Input label="Email" name="email" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} error={errors.email} disabled={isSubmitting} />
        <PasswordInput label="Password" name="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} error={errors.password} disabled={isSubmitting} />
        <Button className="w-full" type="submit" loading={isSubmitting}>{isSubmitting ? "Signing in…" : "Log in"}</Button>
      </form>
      <p className="mt-7 text-center text-sm text-slate">Don&apos;t have an account? <Link href="/register" className="font-bold text-ink underline decoration-coral decoration-2 underline-offset-4">Register</Link></p>
    </div>
  );
}
