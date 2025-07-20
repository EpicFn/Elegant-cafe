"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useUser } from "@/src/store/auth";
import CompleteModal from "@/src/components/features/order/CompleteModal";

interface AuthGuardProps {
  children: React.ReactNode;
  requireAuth?: boolean; // true: 로그인 필요, false: 로그아웃 상태에서만 접근 가능
}

// 로그인 여부 확인 및 페이지 접근 제어
export function AuthGuard({ children, requireAuth = true }: AuthGuardProps) {
  const { isAuthenticated, isInitialized } = useUser();
  const router = useRouter();
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  useEffect(() => {
    // 초기화가 완료된 후에만 인증 상태 확인
    if (!isInitialized) return;

    if (requireAuth && !isAuthenticated) {
      // 로그인이 필요한 페이지인데 로그인되지 않은 경우
      setCompleteMessage("로그인이 필요한 페이지입니다.");
      setCompleteOpen(true);
    } else if (!requireAuth && isAuthenticated) {
      // 로그아웃 상태에서만 접근 가능한 페이지인데 로그인된 경우
      router.push("/");
    }
  }, [isAuthenticated, isInitialized, requireAuth, router]);

  // 초기화 중이거나 인증 상태가 맞지 않으면 로딩 표시
  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-amber-600 mx-auto mb-4"></div>
          <p className="text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  // 인증 상태가 맞지 않으면 아무것도 렌더링하지 않음
  if (requireAuth && !isAuthenticated) {
    return (
      <>
        <CompleteModal
          open={completeOpen}
          onClose={() => {
            setCompleteOpen(false);
            router.push("/");
          }}
          message={completeMessage}
        />
      </>
    );
  }

  if (!requireAuth && isAuthenticated) {
    return null;
  }

  return (
    <>
      {children}
      <CompleteModal
        open={completeOpen}
        onClose={() => {
          setCompleteOpen(false);
          router.push("/");
        }}
        message={completeMessage}
      />
    </>
  );
}
