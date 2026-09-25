import { AuthLayout } from "@/components/layout/AuthLayout";
import { RegisterForm } from "@/components/auth/RegisterForm";

export default async function RegisterPage({ searchParams }: { searchParams: Promise<{ next?: string }> }) {
  const params = await searchParams;
  return <AuthLayout eyebrow="Join the platform" title="Make space for better news." description="Create your account today. We’ll use it to build a more personal, more useful front page over time."><RegisterForm nextPath={params.next} /></AuthLayout>;
}
