"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminRole } from "@/lib/types";
import { Pencil, Plus, Trash2 } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function RolesContent() {
  const { token, hasPermission } = useAuth();
  const [roles, setRoles] = useState<AdminRole[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  useEffect(() => { if (token) adminApi.roles(token).then(setRoles).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load roles.")).finally(() => setLoading(false)); }, [token]);
  async function remove(role: AdminRole) { if (!token || !window.confirm(`Delete ${role.name}?`)) return; try { await adminApi.deleteRole(token, role.id); setRoles((current) => current.filter((item) => item.id !== role.id)); setNotice("Role deleted."); } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not delete role."); } }
  return <AdminShell><div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end"><PageHeader eyebrow="Administration" title="Roles & permissions" description="Create focused access profiles from the system-defined permission catalog." />{hasPermission("ROLE_CREATE") ? <Link href="/admin/roles/new" className="inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-xl bg-ink px-4 text-sm font-semibold text-white hover:bg-ink/90"><Plus size={16} /> Create role</Link> : null}</div>{notice ? <div className="mt-6"><Alert type="success">{notice}</Alert></div> : null}{error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}{loading ? <LoadingState label="Loading roles" /> : <Card className="mt-8 overflow-hidden"><div className="hidden grid-cols-[1fr_130px_120px_90px] gap-4 border-b border-line bg-mist/40 px-6 py-3 text-[10px] font-bold uppercase tracking-[0.16em] text-slate sm:grid"><span>Role</span><span>Type</span><span>Status</span><span /></div>{roles.map((role) => <div key={role.id} className="grid gap-3 border-b border-line px-6 py-5 last:border-0 sm:grid-cols-[1fr_130px_120px_90px] sm:items-center sm:gap-4"><div><Link href={`/admin/roles/${role.id}`} className="font-semibold text-ink hover:text-coral">{role.name}</Link><p className="mt-1 font-mono text-[10px] tracking-wide text-slate">{role.code}</p></div><div><Badge className={role.systemRole ? "bg-ink text-white" : "bg-mist text-slate"}>{role.systemRole ? "System" : "Custom"}</Badge></div><div><Badge className={role.status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-700"}>{role.status === "ACTIVE" ? "Active" : "Inactive"}</Badge></div><div className="flex gap-1 sm:justify-end">{hasPermission("ROLE_EDIT") ? <Link href={`/admin/roles/${role.id}`} aria-label={`Edit ${role.name}`} className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-mist hover:text-ink"><Pencil size={15} /></Link> : null}{!role.systemRole && hasPermission("ROLE_DELETE") ? <button type="button" aria-label={`Delete ${role.name}`} onClick={() => remove(role)} className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-red-50 hover:text-red-700"><Trash2 size={15} /></button> : null}</div></div>)}</Card>}</AdminShell>;
}

export default function RolesPage() { return <AdminRoute><PermissionRoute permission="ROLE_VIEW"><RolesContent /></PermissionRoute></AdminRoute>; }
