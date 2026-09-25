"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { Alert } from "@/components/ui/Alert";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { RoleForm } from "@/components/admin/RoleForm";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminRole } from "@/lib/types";
import { use } from "react";
import { useAuth } from "@/components/auth/AuthProvider";
import { useEffect, useState } from "react";

function EditRoleContent({ id }: { id: string }) {
  const { token } = useAuth();
  const [role, setRole] = useState<AdminRole | null>(null);
  const [error, setError] = useState("");
  useEffect(() => { if (token) adminApi.role(token, id).then(setRole).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load role.")); }, [token, id]);
  if (error) return <AdminShell><Alert>{error}</Alert></AdminShell>;
  if (!role) return <AdminShell><LoadingState label="Loading role" /></AdminShell>;
  return <AdminShell><PageHeader eyebrow="Roles & permissions" title={role.name} description={role.systemRole ? "This is a protected system role." : "Update the role profile and its effective permissions."} /><div className="mt-8 max-w-5xl"><RoleForm initialRole={role} /></div></AdminShell>;
}

export default function EditRolePage({ params }: { params: Promise<{ id: string }> }) { const { id } = use(params); return <AdminRoute><PermissionRoute permission="ROLE_VIEW"><EditRoleContent id={id} /></PermissionRoute></AdminRoute>; }
