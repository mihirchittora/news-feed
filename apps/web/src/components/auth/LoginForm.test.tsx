import { LoginForm } from "@/components/auth/LoginForm";
import { ApiClientError } from "@/lib/api/client";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mocks = vi.hoisted(() => ({
  login: vi.fn(),
  replace: vi.fn(),
}));

vi.mock("@/components/auth/AuthProvider", () => ({
  useAuth: () => ({ login: mocks.login }),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: mocks.replace }),
}));

describe("LoginForm", () => {
  beforeEach(() => {
    mocks.login.mockReset();
    mocks.replace.mockReset();
  });

  it("shows client-side validation errors", async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(screen.getByText("Email is required")).toBeInTheDocument();
    expect(screen.getByText("Password is required")).toBeInTheDocument();
    expect(mocks.login).not.toHaveBeenCalled();
  });

  it("redirects a user after a successful login", async () => {
    mocks.login.mockResolvedValue({ role: "USER" });
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText("Email"), "reader@example.com");
    await user.type(screen.getByLabelText("Password"), "Password123");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => expect(mocks.replace).toHaveBeenCalledWith("/"));
  });

  it("redirects an admin to the protected dashboard", async () => {
    mocks.login.mockResolvedValue({ role: "ADMIN" });
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText("Email"), "admin@example.com");
    await user.type(screen.getByLabelText("Password"), "Password123");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => expect(mocks.replace).toHaveBeenCalledWith("/admin/dashboard"));
  });

  it("lets readers show and hide their password", async () => {
    const user = userEvent.setup();
    render(<LoginForm />);
    const password = screen.getByLabelText("Password");

    expect(password).toHaveAttribute("type", "password");
    await user.click(screen.getByRole("button", { name: "Show password" }));
    expect(password).toHaveAttribute("type", "text");
    await user.click(screen.getByRole("button", { name: "Hide password" }));
    expect(password).toHaveAttribute("type", "password");
  });

  it("renders an API error without exposing implementation details", async () => {
    mocks.login.mockRejectedValue(new ApiClientError({ message: "Invalid email or password" }, 401));
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText("Email"), "reader@example.com");
    await user.type(screen.getByLabelText("Password"), "wrongpass");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Invalid email or password");
  });
});
