import { render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { BreakingNewsSection } from "@/components/public/BreakingNewsSection";

const mocks = vi.hoisted(() => ({ listPublic: vi.fn() }));

vi.mock("@/lib/api/breaking-news", () => ({ breakingNewsApi: { listPublic: mocks.listPublic } }));
vi.mock("next/link", () => ({ default: ({ children, href }: { children: React.ReactNode; href: string }) => <a href={href}>{children}</a> }));

describe("BreakingNewsSection", () => {
  beforeEach(() => mocks.listPublic.mockReset());

  it("renders multiple active stories with the reusable badge", async () => {
    mocks.listPublic.mockResolvedValue({ items: [
      { id: "1", slug: "rain-warning", title: "Rain warning issued", category: { id: "c", name: "Local", slug: "local" }, media: [], publishedAt: "2026-09-25T09:00:00Z", breakingStartedAt: new Date().toISOString() },
      { id: "2", slug: "traffic-update", title: "Traffic update", category: { id: "c", name: "Local", slug: "local" }, media: [], publishedAt: "2026-09-25T08:00:00Z", breakingStartedAt: new Date().toISOString() },
    ] });

    render(<BreakingNewsSection />);

    expect(await screen.findByRole("heading", { name: "Breaking News" })).toBeInTheDocument();
    expect(screen.getByText("Rain warning issued")).toBeInTheDocument();
    expect(screen.getByText("Traffic update")).toBeInTheDocument();
    expect(screen.getByText("Breaking")).toBeInTheDocument();
  });

  it("does not render a large empty section", async () => {
    mocks.listPublic.mockResolvedValue({ items: [] });

    render(<BreakingNewsSection />);

    await waitFor(() => expect(screen.queryByRole("heading", { name: "Breaking News" })).not.toBeInTheDocument());
  });
});
