"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { StaffUser } from "@/lib/types";
import { Plus, Search } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function StaffContent() {
  const { token, hasPermission } = useAuth();
  const [staff, setStaff] = useState<StaffUser[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => { if (!token) return; setLoading(true); adminApi.staff(token, search).then(setStaff).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load staff users.")).finally(() => setLoading(false)); }, [token, search]);
  return <AdminShell><div className="flex flex-col justify-between gap-5 border-b border-line pb-8 sm:flex-row sm:items-end"><PageHeader eyebrow="Administration" title="Staff users" description="Manage staff access, roles, and account status from one place." />{hasPermission("STAFF_CREATE") ? <Link href="/admin/staff/new" className="inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-xl bg-ink px-4 text-sm font-semibold text-white hover:bg-ink/90"><Plus size={16} /> Add staff</Link> : null}</div><div className="mt-7 max-w-xl"><div className="relative"><Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate" size={17} aria-hidden="true" /><input value={search} onChange={(event) => setSearch(event.target.value)} className="min-h-12 w-full rounded-xl border border-line bg-white pl-11 pr-4 text-sm text-ink outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="Search name or email" aria-label="Search staff" /></div></div>{error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}{loading ? <LoadingState label="Loading staff users" /> : <Card className="mt-7 overflow-hidden"><div className="hidden grid-cols-[1fr_1fr_180px_120px] gap-4 border-b border-line bg-mist/40 px-6 py-3 text-[10px] font-bold uppercase tracking-[0.16em] text-slate sm:grid"><span>Name</span><span>Email</span><span>Roles</span><span>Status</span></div>{staff.length === 0 ? <div className="px-6 py-10 text-center text-sm text-slate">No staff users match this search.</div> : staff.map((person) => <Link key={person.id} href={`/admin/staff/${person.id}`} className="grid gap-2 border-b border-line px-6 py-5 last:border-0 hover:bg-mist/30 sm:grid-cols-[1fr_1fr_180px_120px] sm:items-center sm:gap-4"><div><p className="font-semibold text-ink">{person.name}</p><p className="mt-1 text-xs text-slate sm:hidden">{person.email}</p></div><p className="hidden text-sm text-slate sm:block">{person.email}</p><div className="flex flex-wrap gap-1.5">{person.roles.filter((role) => role.code !== "USER").map((role) => <Badge key={role.id}>{role.name}</Badge>)}{person.roles.every((role) => role.code === "USER") ? <span className="text-xs text-slate">User</span> : null}</div><Badge className={person.status === "ACTIVE" ? "bg-emerald-50 text-emerald-700" : person.status === "PENDING_SETUP" ? "bg-amber-50 text-amber-700" : "bg-red-50 text-red-700"}>{person.status === "PENDING_SETUP" ? "Pending setup" : person.status[0] + person.status.slice(1).toLowerCase()}</Badge></Link>)}</Card>}</AdminShell>;
}

export default function StaffPage() { return <AdminRoute><PermissionRoute permission="STAFF_VIEW"><StaffContent /></PermissionRoute></AdminRoute>; }
