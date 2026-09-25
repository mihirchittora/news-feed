FROM node:22.14.0-alpine3.21 AS dependencies

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:22.14.0-alpine3.21 AS builder

WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
ARG NEXT_PUBLIC_API_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ARG NEXT_PUBLIC_FEED_AD_INTERVAL=5
ENV NEXT_PUBLIC_FEED_AD_INTERVAL=${NEXT_PUBLIC_FEED_AD_INTERVAL}
ARG NEXT_PUBLIC_SITE_URL=http://localhost:3000
ENV NEXT_PUBLIC_SITE_URL=${NEXT_PUBLIC_SITE_URL}
COPY --from=dependencies /app/node_modules ./node_modules
COPY package.json package-lock.json next.config.mjs postcss.config.mjs tailwind.config.ts tsconfig.json next-env.d.ts ./
COPY src ./src
RUN mkdir -p public
RUN npm run build

FROM node:22.14.0-alpine3.21 AS runner

RUN addgroup -S nextjs && adduser -S nextjs -G nextjs

WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME=0.0.0.0
COPY --from=builder --chown=nextjs:nextjs /app/public ./public
COPY --from=builder --chown=nextjs:nextjs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nextjs /app/.next/static ./.next/static
USER nextjs
EXPOSE 3000
CMD ["node", "server.js"]
