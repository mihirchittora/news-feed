"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { PageHeader } from "@/components/ui/PageHeader";
import { RoleForm } from "@/components/admin/RoleForm";

function CreateRoleContent() { return <AdminShell><PageHeader eyebrow="Roles & permissions" title="Create role" description="Give a team member exactly the access their work requires." /><div className="mt-8 max-w-5xl"><RoleForm /></div></AdminShell>; }
export default function CreateRolePage() { return <AdminRoute><PermissionRoute permission="ROLE_CREATE"><CreateRoleContent /></PermissionRoute></AdminRoute>; }
