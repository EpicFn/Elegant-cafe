import { useRouter } from "next/navigation";
import { LogIn } from "lucide-react";
import { useState } from "react";
import { Modal } from "@/src/components/common/Modal";
import { ModalContent } from "@/src/components/common/ModalContent";
import { Input } from "@/src/components/common/Input";
import { PrimaryButton } from "@/src/components/common/PrimaryButton";
import { useUser } from "@/src/store/auth";
import { PasswordInput } from "@/src/components/common/PasswordInput";
import CompleteModal from "@/src/components/features/order/CompleteModal";

export function LoginModal({
  onClose,
  onLoginSuccess,
}: {
  onClose: () => void;
  onLoginSuccess: () => void;
}) {
  const router = useRouter();
  const { login } = useUser();
  // 폼 입력 상태
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  // 로그인 폼 제출 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      // UserContext의 login 함수 호출
      await login(email, password);
      // 성공 시 콜백 실행 (예: 모달 닫기, 라우팅 등)
      onLoginSuccess();
    } catch (error) {
      // 실패 시 에러 처리
      console.error("로그인 실패:", error);
      setCompleteMessage("로그인에 실패했습니다. 다시 시도해주세요.");
      setCompleteOpen(true);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal onClose={onClose}>
      <ModalContent>
        <div className="flex flex-col items-center mb-6">
          <LogIn className="h-10 w-10 text-amber-600 mb-2" />
          <h2 className="text-xl font-bold text-center text-gray-800">
            로그인
          </h2>
        </div>
        {/* 로그인 폼 */}
        <form onSubmit={handleSubmit} className="space-y-5">
          <Input
            placeholder="이메일"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <PasswordInput
            placeholder="비밀번호"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <PrimaryButton type="submit" disabled={isLoading}>
            {isLoading ? "로그인 중..." : "로그인"}
          </PrimaryButton>
        </form>
        {/* 회원가입 안내 */}
        <div className="mt-8 text-center text-sm text-gray-600">
          아직 회원이 아니신가요?
          <button
            type="button"
            className="text-amber-600 font-semibold hover:underline ml-1"
            onClick={() => router.replace("/member/signup")}
          >
            회원가입
          </button>
        </div>
      </ModalContent>
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={() => setCompleteOpen(false)}
        message={completeMessage}
      />
    </Modal>
  );
}
