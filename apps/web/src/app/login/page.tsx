import { AuthLayout } from "@/components/layout/AuthLayout";
import { LoginForm } from "@/components/auth/LoginForm";

export default async function LoginPage({ searchParams }: { searchParams: Promise<{ registered?: string; next?: string }> }) {
  const params = await searchParams;
  return <AuthLayout eyebrow="Welcome back" title="Keep up with what matters." description="Sign in to your News Platform account. Your reading experience starts here."><LoginForm registrationSuccess={params.registered === "1"} nextPath={params.next} /></AuthLayout>;
}
