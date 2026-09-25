import { BrandMark } from "@/components/layout/BrandMark";

export function AuthLayout({ eyebrow, title, description, children }: {
  eyebrow: string;
  title: string;
  description: string;
  children: React.ReactNode;
}) {
  return (
    <main className="min-h-[calc(100vh-73px)] bg-paper px-5 py-10 sm:py-16">
      <div className="mx-auto grid max-w-5xl overflow-hidden rounded-3xl border border-line bg-white shadow-soft lg:grid-cols-[0.9fr_1.1fr]">
        <div className="flex flex-col justify-between bg-ink p-7 text-white sm:p-10 lg:p-12">
          <div>
            <BrandMark inverse />
            <p className="mt-14 text-xs font-bold uppercase tracking-[0.24em] text-coral">{eyebrow}</p>
            <h1 className="mt-4 max-w-sm font-display text-4xl font-bold leading-[1.05] tracking-[-0.04em] sm:text-5xl">{title}</h1>
            <p className="mt-5 max-w-sm text-sm leading-6 text-white/65">{description}</p>
          </div>
          <p className="mt-16 hidden text-xs leading-5 text-white/45 lg:block">A considered home for the stories that shape your day.</p>
        </div>
        <div className="p-7 sm:p-10 lg:p-12">{children}</div>
      </div>
    </main>
  );
}
