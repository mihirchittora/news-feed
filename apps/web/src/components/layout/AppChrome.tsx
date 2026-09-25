"use client";

import { PublicHeader } from "@/components/layout/PublicHeader";
import { usePathname } from "next/navigation";

export function AppChrome() {
  const pathname = usePathname();
  if (pathname?.startsWith("/admin")) return null;
  return <PublicHeader />;
}
