import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { FeedCard } from "@/components/public/FeedCard";
import { AuthProvider } from "@/components/auth/AuthProvider";

vi.mock("next/link", () => ({ default: ({ children, href }: { children: React.ReactNode; href: string }) => <a href={href}>{children}</a> }));

describe("FeedCard", () => {
  it("renders text-only stories as readable links", () => {
    render(<AuthProvider><FeedCard story={{ id: "1", title: "Road closure announced", slug: "road-closure-announced", summary: "Plan another route.", category: { id: "c", name: "Local", slug: "local" }, tags: [], media: [], publishedAt: "2026-09-25T07:00:00Z", authorName: "Staff", isBreaking: false, likeCount: 124, commentCount: 18, likedByCurrentUser: false }} /></AuthProvider>);
    expect(screen.getByRole("heading", { name: "Road closure announced" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Road closure announced/ })).toHaveAttribute("href", "/story/road-closure-announced");
  });
});
