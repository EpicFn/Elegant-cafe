export function ModalContent({
  children,
  size = "base", // base | large
  className = "",
}: {
  children: React.ReactNode;
  size?: "base" | "large";
  className?: string;
}) {
  const base = "p-8 bg-white rounded-lg";
  const width = size === "large" ? "w-full max-w-7xl" : "w-[480px]";
  return <div className={`${base} ${width} ${className}`}>{children}</div>;
}
