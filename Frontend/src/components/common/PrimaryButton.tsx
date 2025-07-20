export function PrimaryButton({
  children,
  className,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`w-full bg-amber-600 hover:bg-amber-700 text-white py-3 rounded transition font-semibold ${
        className || ""
      }`}
    >
      {children}
    </button>
  );
}
