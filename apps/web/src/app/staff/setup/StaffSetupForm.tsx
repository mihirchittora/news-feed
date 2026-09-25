"use client";

import { AuthLayout } from "@/components/layout/AuthLayout";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { PasswordInput } from "@/components/ui/PasswordInput";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import { useRouter } from "next/navigation";
import { useState } from "react";

export function StaffSetupForm({ token }: { token: string }) {
  const router = useRouter();
  const [password, setPassword] = useState(""); const [confirm, setConfirm] = useState(""); const [saving, setSaving] = useState(false); const [error, setError] = useState(""); const [done, setDone] = useState(false);
  async function submit(event: React.FormEvent) { event.preventDefault(); setError(""); if (!token) { setError("This setup link is missing its token."); return; } if (password !== confirm) { setError("Passwords do not match."); return; } setSaving(true); try { await adminApi.completeStaffSetup({ token, password }); setDone(true); } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not activate your account."); } finally { setSaving(false); } }
  return <AuthLayout eyebrow="Staff setup" title="Make this workspace yours." description="Set a password to activate your staff account. This one-time setup link expires after 24 hours.">{done ? <div className="space-y-5"><Alert type="success">Your staff account is active. You can now sign in.</Alert><Button type="button" onClick={() => router.replace("/login")}>Go to login</Button></div> : <form onSubmit={submit} className="space-y-5">{error ? <Alert>{error}</Alert> : null}<PasswordInput label="Password" value={password} onChange={(event) => setPassword(event.target.value)} minLength={8} required /><PasswordInput label="Confirm password" value={confirm} onChange={(event) => setConfirm(event.target.value)} minLength={8} required /><Button type="submit" loading={saving} className="mt-2 w-full">Activate account</Button></form>}</AuthLayout>;
}
