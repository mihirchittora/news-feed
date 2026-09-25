"use client";

import { BookOpen, FileText, LayoutDashboard, MessageSquare, Megaphone, Radio, Settings2, Tags, Users, X } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/components/auth/AuthProvider";

const navItems = [
  { label: "Dashboard", icon: LayoutDashboard, href: "/admin/dashboard", permission: null },
  { label: "Stories", icon: FileText, href: "/admin/stories", permission: "STORY_VIEW_ADMIN" },
  { label: "Breaking News", icon: Radio, href: "/admin/breaking-news", permission: "BREAKING_NEWS_MANAGE" },
  { label: "Categories", icon: Tags, href: "/admin/categories", permission: "CATEGORY_MANAGE" },
  { label: "Tags", icon: Tags, href: "/admin/tags", permission: "TAG_MANAGE" },
  { label: "Comments", icon: MessageSquare, href: "/admin/comments", permission: "COMMENT_MODERATE" },
  { label: "Newspaper", icon: BookOpen, href: "/admin/newspapers", permission: "NEWSPAPER_VIEW_ADMIN" },
  { label: "Advertisements", icon: Megaphone, href: "#", permission: "AD_VIEW_ADMIN" },
  { label: "Staff Users", icon: Users, href: "/admin/staff", permission: "STAFF_VIEW" },
  { label: "Roles & Permissions", icon: Settings2, href: "/admin/roles", permission: "ROLE_VIEW" },
];

type AdminSidebarProps = {
  open: boolean;
  onClose: () => void;
  onLogout: () => void;
};

export function AdminSidebar({ open, onClose, onLogout }: AdminSidebarProps) {
  const pathname = usePathname();
  const { hasPermission } = useAuth();
  const visibleItems = navItems.filter((item) => item.permission === null || hasPermission(item.permission));
  return (
    <div className={`fixed inset-0 z-40 lg:static lg:inset-auto lg:block ${open ? "" : "pointer-events-none invisible lg:pointer-events-auto lg:visible"}`}>
      <button type="button" aria-label="Close admin navigation" className={`absolute inset-0 bg-ink/35 transition-opacity lg:hidden ${open ? "opacity-100" : "opacity-0"}`} onClick={onClose} />
      <aside className={`absolute bottom-0 left-0 top-0 flex w-[min(84vw,17rem)] flex-col border-r border-line bg-white px-4 py-6 shadow-soft transition-transform lg:relative lg:w-60 lg:translate-x-0 lg:shadow-none ${open ? "translate-x-0" : "-translate-x-full"}`} aria-label="Admin navigation">
        <div className="flex items-center justify-between px-3 lg:hidden">
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-coral">Workspace</p>
          <button type="button" className="grid h-9 w-9 place-items-center rounded-lg text-slate hover:bg-mist hover:text-ink" onClick={onClose} aria-label="Close admin navigation"><X size={18} aria-hidden="true" /></button>
        </div>
        <p className="hidden px-3 text-[10px] font-bold uppercase tracking-[0.22em] text-slate lg:block">Workspace</p>
        <nav className="mt-4 space-y-1" aria-label="Admin sections">
          {visibleItems.map(({ label, icon: Icon, href }) => href === "#" ? (
            <button key={label} type="button" disabled title={`${label} coming soon`} className="flex min-h-11 w-full cursor-not-allowed items-center gap-3 rounded-xl px-3 text-left text-sm font-semibold text-slate/55">
              <Icon size={17} aria-hidden="true" />{label}<span className="ml-auto text-[9px] font-bold uppercase tracking-wide">Soon</span>
            </button>
          ) : (
            <Link key={label} href={href} onClick={onClose} className={`flex min-h-11 items-center gap-3 rounded-xl px-3 text-left text-sm font-semibold focus:outline-none focus:ring-4 focus:ring-coral/20 ${pathname === href || (href !== "/admin/dashboard" && pathname.startsWith(href)) ? "bg-ink text-white" : "text-slate hover:bg-mist hover:text-ink"}`}>
              <Icon size={17} aria-hidden="true" />{label}
            </Link>
          ))}
        </nav>
        <div className="mt-auto border-t border-line pt-5">
          <button type="button" onClick={onLogout} className="flex min-h-11 w-full items-center gap-3 rounded-xl px-3 text-left text-sm font-semibold text-slate transition hover:bg-mist hover:text-ink focus:outline-none focus:ring-4 focus:ring-coral/20">
            <span className="grid h-5 w-5 place-items-center rounded-full border border-current text-[10px]" aria-hidden="true">↪</span>
            Log out
          </button>
        </div>
      </aside>
    </div>
  );
}
