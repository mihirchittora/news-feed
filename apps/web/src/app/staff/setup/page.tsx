import { StaffSetupForm } from "@/app/staff/setup/StaffSetupForm";

export default async function StaffSetupPage({ searchParams }: { searchParams: Promise<{ token?: string }> }) {
  const params = await searchParams;
  return <StaffSetupForm token={params.token ?? ""} />;
}
