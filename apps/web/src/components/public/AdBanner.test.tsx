import { AdBanner } from "@/components/public/AdBanner";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ ads: vi.fn() }));
vi.mock("@/lib/api/public", () => ({ publicApi: { ads: mocks.ads } }));

describe("AdBanner", () => {
  beforeEach(() => mocks.ads.mockReset());

  it("renders eligible creative with clear sponsored labeling", async () => {
    mocks.ads.mockResolvedValue([{ id: "ad-1", title: "Summer offer", advertiserName: "ABC Properties", description: "A special offer.", mediaType: "IMAGE", mediaUrl: "https://cdn.example.com/ad.jpg", destinationUrl: "https://example.com", placementType: "HOME_BANNER" }]);
    render(<AdBanner placement="HOME_BANNER" />);

    expect(await screen.findByText("Sponsored")).toBeInTheDocument();
    expect(screen.getByText("ABC Properties")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Summer offer/i })).toHaveAttribute("href", "https://example.com");
  });
});
