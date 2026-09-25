type PageHeaderProps = {
  eyebrow?: string;
  title: string;
  description?: string;
};

export function PageHeader({ eyebrow, title, description }: PageHeaderProps) {
  return (
    <header className="max-w-2xl">
      {eyebrow ? <p className="text-xs font-bold uppercase tracking-[0.22em] text-coral">{eyebrow}</p> : null}
      <h1 className="mt-3 font-display text-5xl font-bold tracking-[-0.05em] text-ink">{title}</h1>
      {description ? <p className="mt-4 text-base leading-7 text-slate">{description}</p> : null}
    </header>
  );
}
