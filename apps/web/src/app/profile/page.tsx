"use client";

import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/components/auth/AuthProvider";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { PageContainer } from "@/components/ui/PageContainer";
import { PageHeader } from "@/components/ui/PageHeader";
import { LogOut } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";

function ProfileContent() {
  const { user, logout } = useAuth();
  const router = useRouter();
  if (!user) return null;

  function handleLogout() {
    logout();
    router.replace("/login");
  }

  return (
    <PageContainer className="max-w-4xl">
      <PageHeader eyebrow="Account" title="Your profile" description="Your account details and access level, all in one place." />
      <Card className="mt-10 overflow-hidden">
        <div className="flex items-center gap-4 border-b border-line bg-mist/50 px-6 py-5 sm:px-8"><div className="grid h-12 w-12 place-items-center rounded-2xl bg-coral font-display text-xl font-bold text-white">{user.name.charAt(0).toUpperCase()}</div><div><p className="font-bold text-ink">{user.name}</p><p className="text-sm text-slate">{user.roles?.filter((role) => role.code !== "USER").map((role) => role.name).join(" · ") || (user.role === "ADMIN" ? "Administrator" : "Member")}</p></div></div>
        <dl className="divide-y divide-line px-6 sm:px-8">
          <div className="grid gap-1 py-5 sm:grid-cols-[130px_1fr] sm:gap-5"><dt className="text-xs font-bold uppercase tracking-[0.16em] text-slate">Name</dt><dd className="text-sm font-semibold text-ink">{user.name}</dd></div>
          <div className="grid gap-1 py-5 sm:grid-cols-[130px_1fr] sm:gap-5"><dt className="text-xs font-bold uppercase tracking-[0.16em] text-slate">Email</dt><dd className="text-sm font-semibold text-ink">{user.email}</dd></div>
          <div className="grid gap-1 py-5 sm:grid-cols-[130px_1fr] sm:gap-5"><dt className="text-xs font-bold uppercase tracking-[0.16em] text-slate">Permissions</dt><dd className="flex flex-wrap gap-1.5">{(user.permissions ?? []).length ? user.permissions?.map((permission) => <span key={permission} className="rounded-full bg-mist px-2.5 py-1 font-mono text-[10px] text-slate">{permission}</span>) : <span className="text-sm text-slate">No administrative permissions</span>}</dd></div>
        </dl>
        <div className="flex flex-wrap gap-3 border-t border-line px-6 py-6 sm:px-8"><Button type="button" onClick={handleLogout} className="bg-white !text-ink ring-1 ring-line hover:bg-mist"><LogOut size={16} aria-hidden="true" /> Log out</Button>{user.role === "ADMIN" ? <Link href="/admin/dashboard" className="inline-flex min-h-11 items-center rounded-xl bg-ink px-5 text-sm font-semibold text-white transition hover:bg-ink/90 focus:outline-none focus:ring-4 focus:ring-coral/20">Go to admin dashboard</Link> : null}</div>
      </Card>
    </PageContainer>
  );
}

export default function ProfilePage() {
  return <ProtectedRoute><ProfileContent /></ProtectedRoute>;
}
