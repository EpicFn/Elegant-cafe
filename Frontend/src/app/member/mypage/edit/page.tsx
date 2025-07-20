"use client";

import { useState, useEffect } from "react";
import { Input } from "@/src/components/common/Input";
import { useRouter } from "next/navigation";
import PasswordChangeSection from "@/src/components/features/mypage/PasswordChangeSection";
import Button from "@/src/components/common/Button";
import { useUser } from "@/src/store/auth";
import { AuthGuard } from "@/src/components/common/AuthGuard";
import CompleteModal from "@/src/components/features/order/CompleteModal";
import { AuthService } from "@/src/services/authService";
import {
  validatePassword,
  validatePasswordConfirm,
} from "@/src/utils/validation";

export default function EditProfilePage() {
  const router = useRouter();
  const { user, fetchUserInfo, updateUserInfo } = useUser();

  // 폼 입력값 상태
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [isPasswordVerified, setIsPasswordVerified] = useState(false);
  const [isVerifying, setIsVerifying] = useState(false);
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  // 사용자 정보 초기화
  useEffect(() => {
    if (user) {
      // 사용자 정보가 있으면 name 초기값 설정
      setName(user.name);
    } else {
      // 사용자 정보가 없으면 fetch
      fetchUserInfo();
    }
  }, [user, fetchUserInfo]);

  // 현재 비밀번호 검증
  const verifyCurrentPassword = async () => {
    if (!currentPassword.trim()) {
      setCompleteMessage("현재 비밀번호를 입력해 주세요.");
      setCompleteOpen(true);
      return;
    }

    setIsVerifying(true);
    try {
      const isValid = await AuthService.verifyPassword(currentPassword);
      if (isValid) {
        setIsPasswordVerified(true);
        setCompleteMessage("비밀번호가 확인되었습니다.");
        setCompleteOpen(true);
      } else {
        setIsPasswordVerified(false);
        setCompleteMessage("현재 비밀번호가 일치하지 않습니다.");
        setCompleteOpen(true);
      }
    } catch (error) {
      console.error("비밀번호 검증 실패:", error);
      setCompleteMessage("비밀번호 검증에 실패했습니다.");
      setCompleteOpen(true);
      setIsPasswordVerified(false);
    } finally {
      setIsVerifying(false);
    }
  };

  // 회원 정보 수정 핸들러
  const handleSubmit = async () => {
    // 비밀번호 검증 체크
    if (!isPasswordVerified) {
      setCompleteMessage("현재 비밀번호 인증을 먼저 완료해 주세요.");
      setCompleteOpen(true);
      return;
    }

    // 새 비밀번호 입력 시 유효성 검사
    if (password) {
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
    }

    try {
      // UserContext의 updateUserInfo 함수 호출
      // 비밀번호 변경 시에는 새 비밀번호, 변경하지 않을 때는 현재 비밀번호
      await updateUserInfo({
        name,
        password: password || currentPassword, // 새 비밀번호가 있으면 새 비밀번호, 없으면 현재 비밀번호
      });
      setCompleteMessage("회원 정보가 수정되었습니다.");
      setCompleteOpen(true);
    } catch (error) {
      console.error("회원 정보 수정 오류:", error);
      setCompleteMessage("회원 정보 수정에 실패했습니다. 다시 시도해 주세요.");
      setCompleteOpen(true);
    }
  };

  return (
    <AuthGuard requireAuth={true}>
      <main className="max-w-7xl mx-auto py-12 px-4">
        <h1 className="text-2xl font-semibold mb-6">회원 정보 수정</h1>

        {!user ? (
          <div>사용자 정보를 불러오는 중...</div>
        ) : (
          <section className="bg-white shadow p-8 rounded">
            <table className="w-full table-fixed border-separate border-spacing-y-6">
              <tbody>
                <tr>
                  <th className="text-left align-top text-sm text-gray-600 w-36 pr-4 pt-2">
                    이메일
                  </th>
                  <td>
                    <Input
                      type="email"
                      value={user.email}
                      disabled
                      className="max-w-md bg-gray-100 text-gray-500"
                    />
                  </td>
                </tr>

                <tr>
                  <th className="text-left align-top text-sm text-gray-600 pr-4 pt-2">
                    이름
                  </th>
                  <td>
                    <Input
                      type="text"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      className="max-w-md"
                    />
                  </td>
                </tr>

                <tr>
                  <th className="text-left align-top text-sm text-gray-600 pr-4 pt-2">
                    현재 비밀번호 <span className="text-red-500">*</span>
                  </th>
                  <td>
                    <div className="flex gap-2 items-end">
                      <Input
                        type="password"
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                        placeholder="현재 비밀번호를 입력해 주세요"
                        className="max-w-md"
                        disabled={isPasswordVerified}
                      />
                      <Button
                        text={
                          isVerifying
                            ? "확인 중..."
                            : isPasswordVerified
                            ? "확인됨"
                            : "확인"
                        }
                        onClick={verifyCurrentPassword}
                        disabled={
                          isVerifying ||
                          isPasswordVerified ||
                          !currentPassword.trim()
                        }
                        bgColor={
                          isPasswordVerified ? "bg-green-600" : "bg-amber-600"
                        }
                        hoverColor={
                          isPasswordVerified
                            ? "hover:bg-green-700"
                            : "hover:bg-amber-700"
                        }
                        fontColor="text-white"
                        className="px-6"
                      />
                    </div>
                    <p className="mt-1 text-sm text-gray-500">
                      {isPasswordVerified
                        ? "비밀번호가 확인되었습니다."
                        : "회원 정보 수정을 위해 비밀번호 인증이 필요합니다."}
                    </p>
                  </td>
                </tr>

                <tr>
                  <th className="text-left align-top text-sm text-gray-600 pr-4 pt-2">
                    비밀번호 변경
                  </th>
                  <td>
                    {/* 비밀번호 변경 섹션 */}
                    <PasswordChangeSection
                      password={password}
                      confirmPassword={confirmPassword}
                      setPassword={setPassword}
                      setConfirmPassword={setConfirmPassword}
                    />
                  </td>
                </tr>

                <tr>
                  <th className="text-left align-top text-sm text-gray-600 pr-4 pt-2">
                    기본 배송지
                  </th>
                  <td>
                    <Input
                      type="text"
                      value="등록된 주소가 없습니다."
                      disabled
                      className="max-w-md bg-gray-100 text-gray-500"
                    />
                    <p className="text-sm text-gray-500 mt-2">
                      주소 변경은 마이페이지의 <strong>주소 관리</strong> 탭에서
                      가능합니다.
                    </p>
                  </td>
                </tr>

                <tr>
                  <td colSpan={2} className="pt-8 pr-1">
                    <div className="flex justify-end gap-3">
                      <Button
                        text="취소"
                        onClick={() => router.back()}
                        bgColor="bg-white"
                        fontColor="text-gray-700"
                        hoverColor="hover:bg-gray-100"
                        className="px-6"
                      />
                      <Button
                        text="저장"
                        onClick={handleSubmit}
                        bgColor={
                          isPasswordVerified ? "bg-amber-600" : "bg-gray-400"
                        }
                        hoverColor={
                          isPasswordVerified
                            ? "hover:bg-amber-500"
                            : "hover:bg-gray-400"
                        }
                        fontColor="text-white"
                        className="px-6"
                        disabled={!isPasswordVerified}
                      />
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
        )}
        {/* 완료 모달 */}
        <CompleteModal
          open={completeOpen}
          onClose={() => {
            setCompleteOpen(false);
            if (completeMessage === "회원 정보가 수정되었습니다.") {
              router.back();
            }
          }}
          message={completeMessage}
        />
      </main>
    </AuthGuard>
  );
}
