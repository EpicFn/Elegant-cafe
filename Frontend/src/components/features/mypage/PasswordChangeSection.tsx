"use client";

import { useState, useEffect } from "react";
import { KeyRound, X } from "lucide-react";
import Button from "@/src/components/common/Button";
import { PasswordInput } from "@/src/components/common/PasswordInput";
import {
  validatePassword,
  validatePasswordConfirm,
} from "@/src/utils/validation";

// 비밀번호 변경 섹션: 마이페이지/회원정보수정 등에서 사용
// 편집 모드/입력값 상태 관리, PasswordInput 공통 컴포넌트 사용
interface Props {
  password: string; // 새 비밀번호
  confirmPassword: string; // 새 비밀번호 확인
  setPassword: (v: string) => void;
  setConfirmPassword: (v: string) => void;
}

export default function PasswordChangeSection({
  password,
  confirmPassword,
  setPassword,
  setConfirmPassword,
}: Props) {
  // 편집 모드 상태
  const [isEditing, setIsEditing] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [confirmPasswordError, setConfirmPasswordError] = useState("");

  // 입력값 초기화 및 편집 종료
  const reset = () => {
    setPassword("");
    setConfirmPassword("");
    setIsEditing(false);
    setPasswordError("");
    setConfirmPasswordError("");
  };

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

  // 편집 모드가 아니면 '비밀번호 변경' 버튼만 노출
  if (!isEditing) {
    return (
      <Button
        icon={KeyRound}
        text="비밀번호 변경"
        onClick={() => setIsEditing(true)}
        className="border-amber-600 text-amber-600 hover:bg-amber-50"
      />
    );
  }

  // 편집 모드: 비밀번호 입력 폼 노출
  return (
    <div className="space-y-4">
      <div className="w-full max-w-md">
        <label className="block text-sm text-gray-600 mb-1">새 비밀번호</label>
        {/* 새 비밀번호 입력 */}
        <PasswordInput
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="새 비밀번호를 입력해 주세요"
        />
        {passwordError ? (
          <p className="mt-1 text-sm text-red-500">{passwordError}</p>
        ) : (
          <p className="mt-1 text-sm text-gray-500">8자 이상 입력해 주세요.</p>
        )}
      </div>

      <div className="w-full max-w-md">
        <label className="block text-sm text-gray-600 mb-1">
          비밀번호 확인
        </label>
        {/* 새 비밀번호 확인 입력 */}
        <PasswordInput
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          placeholder="비밀번호를 다시 입력해 주세요"
        />
        {confirmPasswordError && (
          <p className="mt-1 text-sm text-red-500">{confirmPasswordError}</p>
        )}
      </div>

      <div className="pt-2">
        <Button
          icon={X}
          text="취소"
          onClick={reset}
          className="text-sm border-gray-300 text-gray-600 hover:bg-gray-100"
        />
      </div>
    </div>
  );
}
