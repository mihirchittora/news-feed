"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { LoadingState } from "@/components/ui/LoadingState";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminRole, PermissionGroup } from "@/lib/types";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export function RoleForm({ initialRole }: { initialRole?: AdminRole }) {
  const { token, hasPermission } = useAuth();
  const router = useRouter();
  const [groups, setGroups] = useState<PermissionGroup[]>([]);
  const [name, setName] = useState(initialRole?.name ?? "");
  const [code, setCode] = useState(initialRole?.code ?? "");
  const [description, setDescription] = useState(initialRole?.description ?? "");
  const [status, setStatus] = useState(initialRole?.status ?? "ACTIVE");
  const [selected, setSelected] = useState<string[]>(initialRole?.permissionCodes ?? []);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) return;
    adminApi.permissions(token).then(setGroups).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load permissions.")).finally(() => setLoading(false));
  }, [token]);

  function toggle(codeToToggle: string) {
    setSelected((current) => current.includes(codeToToggle) ? current.filter((codeItem) => codeItem !== codeToToggle) : [...current, codeToToggle]);
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (!token) return;
    setSaving(true); setError("");
    try {
      if (initialRole) {
        await adminApi.updateRole(token, initialRole.id, { name, code, description, permissionCodes: selected, status });
        if (!initialRole.systemRole && hasPermission("ROLE_PERMISSION_ASSIGN")) await adminApi.updateRolePermissions(token, initialRole.id, selected);
      }
      else await adminApi.createRole(token, { name, code: code.toUpperCase(), description, permissionCodes: selected });
      router.push("/admin/roles");
    } catch (reason) { setError(reason instanceof ApiClientError ? reason.message : "Could not save this role."); }
    finally { setSaving(false); }
  }

  if (loading) return <LoadingState label="Loading permission catalog" />;
  const protectedRole = Boolean(initialRole?.systemRole);
  return <form onSubmit={submit} className="space-y-6">
    {error ? <Alert>{error}</Alert> : null}
    {protectedRole ? <Alert type="success">SYSTEM ROLE · Protected. System role identity and permissions cannot be changed.</Alert> : null}
    <Card className="p-6 sm:p-8">
      <div className="grid gap-5 sm:grid-cols-2">
        <Input label="Role name" value={name} onChange={(event) => setName(event.target.value)} disabled={protectedRole} required />
        <Input label="Role code" value={code} onChange={(event) => setCode(event.target.value.toUpperCase())} disabled={Boolean(initialRole)} required />
      </div>
      <div className="mt-5 space-y-2"><label className="block text-sm font-semibold text-ink" htmlFor="description">Description</label><textarea id="description" value={description} onChange={(event) => setDescription(event.target.value)} disabled={protectedRole} className="min-h-28 w-full rounded-xl border border-line bg-white px-4 py-3 text-[15px] text-ink outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" placeholder="What this role is responsible for" /></div>
      {initialRole && !protectedRole ? <div className="mt-5 max-w-xs space-y-2"><label className="block text-sm font-semibold text-ink" htmlFor="status">Status</label><select id="status" value={status} onChange={(event) => setStatus(event.target.value as "ACTIVE" | "INACTIVE")} className="min-h-12 w-full rounded-xl border border-line bg-white px-4 text-sm text-ink outline-none focus:border-coral"><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></div> : null}
    </Card>
    <Card className="p-6 sm:p-8">
      <div className="flex items-end justify-between gap-4"><div><p className="text-xs font-bold uppercase tracking-[0.18em] text-coral">Permission catalog</p><h2 className="mt-2 font-display text-2xl font-bold tracking-[-0.03em] text-ink">Choose access</h2></div><span className="text-xs font-semibold text-slate">{selected.length} selected</span></div>
      <div className="mt-6 grid gap-6 md:grid-cols-2">{groups.map((group) => <fieldset key={group.category}><legend className="text-xs font-bold uppercase tracking-[0.16em] text-slate">{group.category}</legend><div className="mt-3 space-y-2">{group.permissions.map((permission) => <label key={permission.code} className={`flex gap-3 rounded-xl border px-3 py-3 ${selected.includes(permission.code) ? "border-coral/40 bg-coral/5" : "border-line bg-white"}`}><input type="checkbox" checked={selected.includes(permission.code)} onChange={() => toggle(permission.code)} disabled={protectedRole || !hasPermission("ROLE_PERMISSION_ASSIGN")} className="mt-0.5 h-4 w-4 accent-[#e36f61]" /><span><span className="block text-sm font-semibold text-ink">{permission.name}</span><span className="mt-0.5 block text-xs leading-5 text-slate">{permission.description}</span><span className="mt-1 block font-mono text-[10px] tracking-wide text-slate/80">{permission.code}</span></span></label>)}</div></fieldset>)}</div>
      {!protectedRole && !hasPermission("ROLE_PERMISSION_ASSIGN") ? <p className="mt-5 text-xs text-slate">You can view this catalog, but assigning permissions requires ROLE_PERMISSION_ASSIGN.</p> : null}
    </Card>
    {!protectedRole ? <div className="flex justify-end gap-3"><Button type="button" className="bg-white !text-ink ring-1 ring-line hover:bg-mist" onClick={() => router.push("/admin/roles")}>Cancel</Button><Button type="submit" loading={saving}>{initialRole ? "Save changes" : "Create role"}</Button></div> : null}
  </form>;
}
