import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { UserPlus } from "lucide-react";
import { Modal } from "@/src/components/common/Modal";
import { ModalContent } from "@/src/components/common/ModalContent";
import { Input } from "@/src/components/common/Input";
import { PrimaryButton } from "@/src/components/common/PrimaryButton";
import { useUser } from "@/src/store/auth";
import { PasswordInput } from "@/src/components/common/PasswordInput";
import CompleteModal from "@/src/components/features/order/CompleteModal";
import {
  validatePassword,
  validatePasswordConfirm,
} from "@/src/utils/validation";

// 회원가입 모달: 폼 입력값을 UserContext의 signup 함수에만 전달
// 실제 회원가입 API 연동/토큰 저장 등은 UserContext에서만 처리
export function SignupModal({
  onClose,
  onSignupSuccess,
}: {
  onClose: () => void;
  onSignupSuccess: () => void;
}) {
  const router = useRouter();
  const { signup } = useUser();
  // 폼 입력값 상태
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [confirmPasswordError, setConfirmPasswordError] = useState("");
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  // 비밀번호 유효성 검사
  useEffect(() => {
    if (password) {
      const validation = validatePassword(password);
      setPasswordError(validation.isValid ? "" : validation.message);
    } else {
      setPasswordError("");
    }
  }, [password]);

  // 비밀번호 확인 유효성 검사
  useEffect(() => {
    if (confirmPassword) {
      const validation = validatePasswordConfirm(password, confirmPassword);
      setConfirmPasswordError(validation.isValid ? "" : validation.message);
    } else {
      setConfirmPasswordError("");
    }
  }, [password, confirmPassword]);

  // 회원가입 폼 제출 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 비밀번호 유효성 검사
    const passwordValidation = validatePassword(password);
    if (!passwordValidation.isValid) {
      setCompleteMessage(passwordValidation.message);
      setCompleteOpen(true);
      return;
    }

    // 비밀번호 확인 검사
    const confirmValidation = validatePasswordConfirm(
      password,
      confirmPassword
    );
    if (!confirmValidation.isValid) {
      setCompleteMessage(confirmValidation.message);
      setCompleteOpen(true);
      return;
    }

    setIsLoading(true);
    try {
      // UserContext의 signup 함수만 호출
      await signup(name, email, password);
      // 성공 시 회원가입 완료 메시지 표시 후 로그인 페이지로 이동
      setCompleteMessage("회원가입이 완료되었습니다.");
      setCompleteOpen(true);
    } catch (error) {
      // 실패 시 에러 처리
      console.error("회원가입 실패:", error);
      setCompleteMessage("회원가입에 실패했습니다. 다시 시도해주세요.");
      setCompleteOpen(true);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal onClose={onClose}>
      <ModalContent>
        <div className="flex flex-col items-center mb-4">
          <UserPlus className="h-10 w-10 text-amber-600 mb-2" />
          <h2 className="text-xl font-bold text-center text-gray-800">
            회원가입
          </h2>
          <p className="text-sm text-gray-500 mt-2">
            우아한 카페에 오신 것을 환영합니다.
          </p>
        </div>
        {/* 회원가입 폼 */}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이름
            </label>
            <Input
              placeholder="이름을 입력해 주세요"
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이메일
            </label>
            <Input
              placeholder="example@cafe.com"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              비밀번호
            </label>
            <PasswordInput
              placeholder="비밀번호를 입력해 주세요"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {passwordError ? (
              <p className="mt-1 text-xs text-red-500">{passwordError}</p>
            ) : (
              <p className="mt-1 text-xs text-gray-500">
                8자 이상 입력해 주세요.
              </p>
            )}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              비밀번호 확인
            </label>
            <PasswordInput
              placeholder="비밀번호를 다시 입력해 주세요"
              required
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
            {confirmPasswordError && (
              <p className="mt-1 text-xs text-red-500">
                {confirmPasswordError}
              </p>
            )}
          </div>
          <PrimaryButton type="submit" disabled={isLoading}>
            {isLoading ? "회원가입 중..." : "회원가입"}
          </PrimaryButton>
        </form>
        {/* 로그인 안내 */}
        <div className="mt-8 text-center text-sm text-gray-600">
          이미 계정이 있으신가요?
          <button
            type="button"
            className="text-amber-600 font-semibold hover:underline ml-1"
            onClick={() => router.replace("/member/login")}
          >
            로그인
          </button>
        </div>
      </ModalContent>
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={() => {
          setCompleteOpen(false);
          if (completeMessage === "회원가입이 완료되었습니다.") {
            onClose(); // 회원가입 모달 닫기
            router.replace("/member/login");
          }
        }}
        message={completeMessage}
      />
    </Modal>
  );
}
