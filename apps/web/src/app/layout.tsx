import type { Metadata } from "next";
import { AuthProvider } from "@/components/auth/AuthProvider";
import { AppChrome } from "@/components/layout/AppChrome";
import "./globals.css";

export const metadata: Metadata = {
  title: "News Platform",
  description: "A thoughtful home for the news that matters.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <AppChrome />
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
