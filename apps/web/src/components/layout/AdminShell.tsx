"use client";

import { AdminSidebar } from "@/components/layout/AdminSidebar";
import { BrandMark } from "@/components/layout/BrandMark";
import { Menu } from "lucide-react";
import { useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";
import { useRouter } from "next/navigation";

export function AdminShell({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [menuOpen, setMenuOpen] = useState(false);
  if (!user) return null;
  return (
    <main className="min-h-screen bg-mist/40">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex max-w-[90rem] items-center justify-between px-5 py-4 lg:px-8">
          <div className="flex items-center gap-3"><BrandMark /><span className="hidden border-l border-line pl-3 text-xs font-bold uppercase tracking-[0.16em] text-slate sm:block">Admin</span></div>
          <div className="flex items-center gap-3">
            <div className="hidden items-center gap-3 rounded-2xl border border-line px-3 py-2 sm:flex"><div className="grid h-8 w-8 place-items-center rounded-xl bg-coral text-sm font-bold text-white">{user.name.charAt(0).toUpperCase()}</div><div><p className="text-xs font-bold text-ink">{user.name}</p><p className="text-[11px] text-slate">{user.roles?.find((role) => role.code !== "USER")?.name ?? "Staff workspace"}</p></div></div>
            <button type="button" className="grid h-10 w-10 place-items-center rounded-xl text-ink hover:bg-mist focus:outline-none focus:ring-4 focus:ring-coral/15 lg:hidden" onClick={() => setMenuOpen(true)} aria-label="Open admin navigation"><Menu size={20} aria-hidden="true" /></button>
          </div>
        </div>
      </header>
      <div className="mx-auto flex max-w-[90rem]">
        <AdminSidebar open={menuOpen} onClose={() => setMenuOpen(false)} onLogout={() => { logout(); router.replace("/login"); }} />
        <div className="min-w-0 flex-1 px-5 py-8 sm:px-8 lg:px-12 lg:py-12">{children}</div>
      </div>
    </main>
  );
}
