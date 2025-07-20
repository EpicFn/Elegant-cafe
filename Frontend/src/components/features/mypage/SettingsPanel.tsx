import Button from "@/src/components/common/Button";
import { Settings, LogOut, UserX } from "lucide-react";
import { useUser } from "@/src/store/auth";
import { useRouter } from "next/navigation";
import { useState } from "react";
import ConfirmModal from "@/src/components/features/order/ConfirmModal";
import CompleteModal from "@/src/components/features/order/CompleteModal";

export default function SettingsPanel() {
  const { clearUser, withdraw } = useUser();
  const router = useRouter();
  // ConfirmModal open 상태 관리
  const [confirmOpen, setConfirmOpen] = useState(false);
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  // 회원 탈퇴 실제 처리
  const handleWithdraw = async () => {
    try {
      // 회원 탈퇴 실행
      await withdraw();
      // 즉시 홈으로 이동하여 AuthGuard 메시지 방지
      router.push("/");
      // 완료 메시지 설정
      setCompleteMessage("회원 탈퇴가 완료되었습니다.");
      setCompleteOpen(true);
      // clearUser 호출
      clearUser();
    } catch (e) {
      console.error("회원 탈퇴 실패:", e);
      setCompleteMessage("회원 탈퇴에 실패했습니다. 다시 시도해 주세요.");
      setCompleteOpen(true);
    }
  };

  // 완료 모달 닫기 핸들러
  const handleCompleteClose = () => {
    setCompleteOpen(false);
  };

  return (
    <section className="bg-white shadow p-6 rounded">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold">설정</h2>
        <Button
          icon={Settings}
          text="설정 초기화"
          className="invisible border-amber-600 text-amber-600 hover:bg-amber-50"
        />
      </div>

      <ul className="space-y-6">
        <li className="flex items-center justify-between pt-6 border-t border-gray-200">
          <p className="text-gray-600">회원 탈퇴</p>
          <Button
            icon={UserX}
            text="회원 탈퇴"
            onClick={() => setConfirmOpen(true)}
            className="border-red-600 text-red-600 hover:bg-red-50"
          />
        </li>
      </ul>
      {/* 회원 탈퇴 확인 모달 */}
      <ConfirmModal
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={() => {
          setConfirmOpen(false);
          handleWithdraw();
        }}
        message={"정말로 회원 탈퇴하시겠습니까?\n이 작업은 되돌릴 수 없습니다."}
      />
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={handleCompleteClose}
        message={completeMessage}
      />
    </section>
  );
}
