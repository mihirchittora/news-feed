import { NewspaperArchive } from "@/components/public/NewspaperArchive";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ newspapers: vi.fn() }));

vi.mock("@/lib/api/public", () => ({ publicApi: { newspapers: mocks.newspapers } }));
vi.mock("next/link", () => ({ default: ({ children, href }: { children: React.ReactNode; href: string }) => <a href={href}>{children}</a> }));

describe("NewspaperArchive", () => {
  beforeEach(() => mocks.newspapers.mockReset());

  it("renders published edition metadata without loading the PDF", async () => {
    mocks.newspapers.mockResolvedValue({ items: [{ id: "edition-1", title: "Daily Newspaper", edition: "Udaipur", editionDate: "2026-09-25", coverImageUrl: null, publishedAt: "2026-09-25T06:00:00Z" }] });
    render(<NewspaperArchive />);

    expect(await screen.findByText("Udaipur Edition")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Read Newspaper" })).toHaveAttribute("href", "/newspaper/edition-1");
  });
});
