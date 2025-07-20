import { Input } from "@/src/components/common/Input";
import { Eye, EyeOff } from "lucide-react";
import { useState } from "react";

// 비밀번호 입력 + 보기/숨기기 토글을 제공하는 공통 컴포넌트
interface PasswordInputProps {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder?: string;
  required?: boolean;
  name?: string;
  autoComplete?: string;
  className?: string;
}

export function PasswordInput({
  value,
  onChange,
  placeholder,
  required,
  name,
  autoComplete,
  className,
}: PasswordInputProps) {
  // show: true면 비밀번호 보이기, false면 숨기기
  const [show, setShow] = useState(false);

  return (
    <div className="relative">
      {/* 실제 비밀번호 입력란 */}
      <Input
        type={show ? "text" : "password"}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        name={name}
        autoComplete={autoComplete}
        className={className ? className + " pr-10" : "pr-10"}
      />
      {/* 보기/숨기기 토글 버튼 */}
      <button
        type="button"
        onClick={() => setShow((prev) => !prev)}
        className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400"
        aria-label={show ? "비밀번호 숨기기" : "비밀번호 보기"}
        tabIndex={-1}
      >
        {show ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
      </button>
    </div>
  );
}
