import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { FeedCard } from "@/components/public/FeedCard";

vi.mock("next/link", () => ({ default: ({ children, href }: { children: React.ReactNode; href: string }) => <a href={href}>{children}</a> }));

describe("FeedCard", () => {
  it("renders text-only stories as readable links", () => {
    render(<FeedCard story={{ id: "1", title: "Road closure announced", slug: "road-closure-announced", summary: "Plan another route.", category: { id: "c", name: "Local", slug: "local" }, tags: [], media: [], publishedAt: "2026-09-25T07:00:00Z", authorName: "Staff" }} />);
    expect(screen.getByRole("heading", { name: "Road closure announced" })).toBeInTheDocument();
    expect(screen.getByRole("link")).toHaveAttribute("href", "/story/road-closure-announced");
  });
});
