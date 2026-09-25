"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminRole, StaffUser } from "@/lib/types";
import { Check, Copy } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function NewStaffContent() {
  const { token } = useAuth();
  const router = useRouter();
  const [roles, setRoles] = useState<AdminRole[]>([]);
  const [name, setName] = useState(""); const [email, setEmail] = useState(""); const [selected, setSelected] = useState<string[]>([]);
  const [created, setCreated] = useState<StaffUser | null>(null); const [copied, setCopied] = useState(false); const [loading, setLoading] = useState(true); const [saving, setSaving] = useState(false); const [error, setError] = useState("");
  useEffect(() => { if (token) adminApi.roles(token).then((items) => setRoles(items.filter((role) => role.code !== "USER"))).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load roles.")).finally(() => setLoading(false)); }, [token]);
  function toggle(id: string) { setSelected((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id]); }
  async function submit(event: React.FormEvent) { event.preventDefault(); if (!token) return; setSaving(true); setError(""); try { setCreated(await adminApi.createStaff(token, { name, email, roleIds: selected })); } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not create staff user."); } finally { setSaving(false); } }
  async function copyLink() { if (!created?.setupLink) return; await navigator.clipboard.writeText(created.setupLink); setCopied(true); setTimeout(() => setCopied(false), 1600); }
  if (loading) return <AdminShell><LoadingState label="Loading roles" /></AdminShell>;
  return <AdminShell><PageHeader eyebrow="Staff users" title="Add staff" description="Create an account with a one-time setup link. No permanent password is created here." /><div className="mt-8 max-w-3xl">{error ? <div className="mb-6"><Alert>{error}</Alert></div> : null}{created ? <Card className="p-6 sm:p-8"><Alert type="success">Staff account created in pending setup.</Alert><div className="mt-6"><p className="text-sm font-bold text-ink">One-time setup link</p><p className="mt-2 break-all rounded-xl bg-mist px-4 py-3 font-mono text-xs leading-5 text-slate">{created.setupLink}</p><p className="mt-3 text-xs leading-5 text-slate">This link expires in 24 hours and can only be used once. It is shown here for development because email delivery is not configured.</p><div className="mt-5 flex flex-wrap gap-3"><Button type="button" onClick={copyLink} className="gap-2">{copied ? <Check size={16} /> : <Copy size={16} />}{copied ? "Copied" : "Copy link"}</Button><Button type="button" onClick={() => router.push(`/admin/staff/${created.id}`)} className="bg-white !text-ink ring-1 ring-line hover:bg-mist">View staff</Button></div></div></Card> : <form onSubmit={submit} className="space-y-6"><Card className="space-y-5 p-6 sm:p-8"><Input label="Name" value={name} onChange={(event) => setName(event.target.value)} required /><Input label="Email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></Card><Card className="p-6 sm:p-8"><div><p className="text-xs font-bold uppercase tracking-[0.18em] text-coral">Role assignments</p><h2 className="mt-2 font-display text-2xl font-bold text-ink">Choose one or more roles</h2></div><div className="mt-5 grid gap-3 sm:grid-cols-2">{roles.map((role) => <label key={role.id} className={`flex gap-3 rounded-xl border px-4 py-3 ${selected.includes(role.id) ? "border-coral/40 bg-coral/5" : "border-line"}`}><input type="checkbox" checked={selected.includes(role.id)} onChange={() => toggle(role.id)} className="mt-0.5 h-4 w-4 accent-[#e36f61]" /><span><span className="block text-sm font-semibold text-ink">{role.name}</span><span className="mt-1 block text-xs font-mono text-slate">{role.code}</span></span></label>)}</div></Card><div className="flex justify-end gap-3"><Button type="button" className="bg-white !text-ink ring-1 ring-line hover:bg-mist" onClick={() => router.push("/admin/staff")}>Cancel</Button><Button type="submit" loading={saving} disabled={selected.length === 0}>Create staff</Button></div></form>}</div></AdminShell>;
}

export default function NewStaffPage() { return <AdminRoute><PermissionRoute permission="STAFF_CREATE"><NewStaffContent /></PermissionRoute></AdminRoute>; }
