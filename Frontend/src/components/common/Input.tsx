export function Input({
  className,
  ...props
}: React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      {...props}
      className={`border border-gray-300 w-full p-3 rounded focus:outline-none focus:ring-2 focus:ring-orange-500 ${
        className || ""
      }`}
    />
  );
}
