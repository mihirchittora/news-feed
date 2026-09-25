"use client";

import { AdminRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card } from "@/components/ui/Card";
import { PageHeader } from "@/components/ui/PageHeader";
import { useAuth } from "@/components/auth/AuthProvider";
import { BookOpen, FileText, MessageSquare, Megaphone, ShieldCheck } from "lucide-react";

const cards = [
  { label: "Stories", copy: "Publishing tools are coming soon.", icon: FileText, tone: "bg-[#eaf1f4]" },
  { label: "Newspaper", copy: "Editions will live here soon.", icon: BookOpen, tone: "bg-[#f6eee8]" },
  { label: "Advertisements", copy: "Campaign management is coming.", icon: Megaphone, tone: "bg-[#f0edf5]" },
  { label: "Comments", copy: "Moderation tools are coming soon.", icon: MessageSquare, tone: "bg-[#eef3eb]" },
];

function DashboardContent() {
  const { user } = useAuth();
  if (!user) return null;
  return <AdminShell>
    <div className="border-b border-line pb-8"><PageHeader eyebrow="Admin workspace" title="Dashboard" description={`Welcome, ${user.name.split(" ")[0]}. Your access is shaped by active roles and permissions.`} /></div>
    <div className="mt-8 flex flex-wrap gap-2">{(user.roles ?? []).map((role) => <span key={role.id} className="rounded-full bg-ink px-3 py-1.5 text-xs font-semibold text-white">{role.name}</span>)}</div>
    <div className="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{cards.map(({ label, copy, icon: Icon, tone }) => <Card key={label} className="rounded-2xl p-5 shadow-sm"><div className={`grid h-11 w-11 place-items-center rounded-xl ${tone} text-ink`}><Icon size={19} aria-hidden="true" /></div><p className="mt-8 text-xs font-bold uppercase tracking-[0.16em] text-slate">{label}</p><p className="mt-2 font-display text-2xl font-bold tracking-[-0.03em] text-ink">Coming soon</p><p className="mt-2 text-xs leading-5 text-slate">{copy}</p></Card>)}</div>
    <div className="mt-8 flex items-start gap-3 rounded-2xl border border-dashed border-line bg-white/60 px-5 py-6 sm:px-7"><ShieldCheck size={18} className="mt-0.5 shrink-0 text-coral" aria-hidden="true" /><div><p className="text-sm font-bold text-ink">Permission-aware workspace.</p><p className="mt-1 text-sm leading-6 text-slate">Staff, roles, and permission changes are live. Stories, newspapers, advertisements, and comments remain intentionally out of scope for this milestone.</p></div></div>
  </AdminShell>;
}

export default function AdminDashboardPage() {
  return <AdminRoute><DashboardContent /></AdminRoute>;
}
