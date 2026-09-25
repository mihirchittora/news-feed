/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  poweredByHeader: false,
  output: "standalone",
  agentRules: false,
  async headers() {
    return [{
      source: "/(.*)",
      headers: [
        { key: "X-Content-Type-Options", value: "nosniff" },
        { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
        { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=(), payment=()" },
        { key: "X-Frame-Options", value: "DENY" },
        // Next's statically prerendered App Router pages include inline hydration
        // bootstrap scripts. Without this directive the shell renders, but React
        // never hydrates, leaving client loading states and forms unusable.
        { key: "Content-Security-Policy", value: "default-src 'self'; script-src 'self' 'unsafe-inline'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'; img-src 'self' data: blob: http: https:; media-src 'self' blob: http: https:; frame-src 'self' blob:; connect-src 'self' http://localhost:8080 https:; object-src 'none'" },
      ],
    }];
  },
};

export default nextConfig;
