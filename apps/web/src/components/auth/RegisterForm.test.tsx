import { RegisterForm } from "@/components/auth/RegisterForm";
import { ApiClientError } from "@/lib/api/client";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mocks = vi.hoisted(() => ({
  register: vi.fn(),
  replace: vi.fn(),
}));

vi.mock("@/components/auth/AuthProvider", () => ({
  useAuth: () => ({ register: mocks.register }),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: mocks.replace }),
}));

describe("RegisterForm", () => {
  beforeEach(() => {
    mocks.register.mockReset();
    mocks.replace.mockReset();
  });

  it("validates password confirmation before submitting", async () => {
    const user = userEvent.setup();
    render(<RegisterForm />);

    await user.type(screen.getByLabelText("Name"), "Reader");
    await user.type(screen.getByLabelText("Email"), "reader@example.com");
    await user.type(screen.getByLabelText("Password"), "Password123");
    await user.type(screen.getByLabelText("Confirm password"), "Different123");
    await user.click(screen.getByRole("button", { name: "Create account" }));

    expect(screen.getByText("Passwords do not match")).toBeInTheDocument();
    expect(mocks.register).not.toHaveBeenCalled();
  });

  it("redirects to login after creating an account", async () => {
    mocks.register.mockResolvedValue({ id: "new-user" });
    const user = userEvent.setup();
    render(<RegisterForm />);

    await user.type(screen.getByLabelText("Name"), "Reader");
    await user.type(screen.getByLabelText("Email"), "reader@example.com");
    await user.type(screen.getByLabelText("Password"), "Password123");
    await user.type(screen.getByLabelText("Confirm password"), "Password123");
    await user.click(screen.getByRole("button", { name: "Create account" }));

    await waitFor(() => expect(mocks.replace).toHaveBeenCalledWith("/login?registered=1"));
  });

  it("shows field errors returned by the API", async () => {
    mocks.register.mockRejectedValue(new ApiClientError({ message: "Request validation failed", errors: { email: "Email is already registered" } }, 400));
    const user = userEvent.setup();
    render(<RegisterForm />);

    await user.type(screen.getByLabelText("Name"), "Reader");
    await user.type(screen.getByLabelText("Email"), "reader@example.com");
    await user.type(screen.getByLabelText("Password"), "Password123");
    await user.type(screen.getByLabelText("Confirm password"), "Password123");
    await user.click(screen.getByRole("button", { name: "Create account" }));

    expect(await screen.findByText("Email is already registered")).toBeInTheDocument();
    expect(screen.getByText("Request validation failed")).toBeInTheDocument();
  });
});
