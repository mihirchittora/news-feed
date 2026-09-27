import type { Metadata } from "next";
import { AuthProvider } from "@/components/auth/AuthProvider";
import { AppChrome } from "@/components/layout/AppChrome";
import Script from "next/script";
import "./globals.css";

export const metadata: Metadata = {
  title: "News Platform",
  description: "A thoughtful home for the news that matters.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <Script src="/runtime-config.js" strategy="beforeInteractive" />
        <AuthProvider>
          <AppChrome />
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
