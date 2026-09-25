import { AuthLayout } from "@/components/layout/AuthLayout";
import { RegisterForm } from "@/components/auth/RegisterForm";

export default function RegisterPage() {
  return <AuthLayout eyebrow="Join the platform" title="Make space for better news." description="Create your account today. We’ll use it to build a more personal, more useful front page over time."><RegisterForm /></AuthLayout>;
}
