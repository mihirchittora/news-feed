import { AdminRoute, ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { render, screen, waitFor } from "@testing-library/react";

const mocks = vi.hoisted(() => ({
  auth: { user: null as { role: "USER" | "ADMIN" } | null, isLoading: false },
  replace: vi.fn(),
}));

vi.mock("@/components/auth/AuthProvider", () => ({
  useAuth: () => mocks.auth,
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: mocks.replace }),
}));

describe("protected routes", () => {
  beforeEach(() => {
    mocks.auth = { user: null, isLoading: false };
    mocks.replace.mockReset();
  });

  it("redirects an anonymous reader to login", async () => {
    render(<ProtectedRoute><p>private profile</p></ProtectedRoute>);

    expect(screen.queryByText("private profile")).not.toBeInTheDocument();
    await waitFor(() => expect(mocks.replace).toHaveBeenCalledWith("/login"));
  });

  it("redirects a signed-in reader away from admin", async () => {
    mocks.auth.user = { role: "USER" };
    render(<AdminRoute><p>admin dashboard</p></AdminRoute>);

    expect(screen.queryByText("admin dashboard")).not.toBeInTheDocument();
    await waitFor(() => expect(mocks.replace).toHaveBeenCalledWith("/403"));
  });

  it("allows an administrator into admin routes", () => {
    mocks.auth.user = { role: "ADMIN" };
    render(<AdminRoute><p>admin dashboard</p></AdminRoute>);

    expect(screen.getByText("admin dashboard")).toBeInTheDocument();
  });
});
