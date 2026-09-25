import type { MetadataRoute } from "next";

export default function robots(): MetadataRoute.Robots {
  const siteUrl = (process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000").replace(/\/$/, "");
  return {
    rules: [{ userAgent: "*", allow: ["/", "/story/", "/category/", "/tag/", "/newspaper"], disallow: ["/admin/", "/api/", "/staff/", "/login", "/register", "/profile", "/newspaper/*/document"] }],
    sitemap: `${siteUrl}/sitemap.xml`,
  };
}
