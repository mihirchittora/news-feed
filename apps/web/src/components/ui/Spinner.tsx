export function Spinner({ size = "md", light = false }: { size?: "sm" | "md"; light?: boolean }) {
  return (
    <span
      className={`inline-block animate-spin rounded-full border-2 ${size === "sm" ? "h-4 w-4" : "h-5 w-5"} ${light ? "border-white/35 border-t-white" : "border-line border-t-coral"}`}
      aria-hidden="true"
    />
  );
}
