"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { BrandMark } from "@/components/layout/BrandMark";
import { Menu, X } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export function PublicHeader() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  useEffect(() => setIsMenuOpen(false), [pathname]);

  function handleLogout() {
    logout();
    setIsMenuOpen(false);
    router.replace("/login");
  }

  return (
    <header className="relative z-30 border-b border-line/80 bg-paper/95">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-5 py-4 lg:px-8">
        <BrandMark />
        <button
          type="button"
          className="grid h-10 w-10 place-items-center rounded-xl text-ink transition hover:bg-mist focus:outline-none focus:ring-4 focus:ring-coral/15 sm:hidden"
          onClick={() => setIsMenuOpen((current) => !current)}
          aria-label={isMenuOpen ? "Close navigation menu" : "Open navigation menu"}
          aria-expanded={isMenuOpen}
          aria-controls="public-navigation"
        >
          {isMenuOpen ? <X size={20} aria-hidden="true" /> : <Menu size={20} aria-hidden="true" />}
        </button>
        <nav id="public-navigation" className={`${isMenuOpen ? "absolute inset-x-0 top-full border-b border-line bg-paper px-5 py-3 shadow-soft" : "hidden"} sm:static sm:block sm:border-0 sm:bg-transparent sm:p-0 sm:shadow-none`} aria-label="Primary navigation">
          {user ? (
            <div className="flex flex-col gap-1 text-sm font-semibold sm:flex-row sm:items-center sm:gap-2">
              <Link href="/profile" className="rounded-lg px-3 py-3 text-slate transition hover:bg-mist hover:text-ink sm:py-2">Profile</Link>
              {user.role === "ADMIN" ? <Link href="/admin/dashboard" className="rounded-lg px-3 py-3 text-slate transition hover:bg-mist hover:text-ink sm:py-2">Admin</Link> : null}
              <button type="button" onClick={handleLogout} className="rounded-lg border border-line px-3 py-3 text-left text-ink transition hover:border-ink sm:py-2 sm:text-center">Log out</button>
            </div>
          ) : (
            <div className="flex flex-col gap-1 text-sm font-semibold sm:flex-row sm:items-center sm:gap-2">
              <Link href="/login" className="rounded-lg px-3 py-3 text-slate transition hover:bg-mist hover:text-ink sm:py-2">Log in</Link>
              <Link href="/register" className="rounded-lg bg-ink px-3.5 py-3 text-white transition hover:bg-ink/90 sm:py-2">Join free</Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
