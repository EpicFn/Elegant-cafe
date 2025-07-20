"use client";
import { useEffect } from "react";

export function Modal({
  children,
  onClose,
  size = "base", // base | large
}: {
  children: React.ReactNode;
  onClose: () => void;
  size?: "base" | "large";
}) {
  useEffect(() => {
    // ESC 키로 모달 닫기
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handleEscape);
    // 모달이 열릴 때 body 스크롤 잠금
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", handleEscape);
      // 모달이 닫힐 때 body 스크롤 해제
      document.body.style.overflow = "unset";
    };
  }, [onClose]);

  const width = size === "large" ? "max-w-7xl w-full" : "w-[480px]";
  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      role="dialog"
      aria-modal="true"
    >
      {/* 바깥 클릭시 닫기 */}
      <div className="absolute inset-0" onClick={onClose} />
      {/* 모달 내용 */}
      <div className={`relative ${width}`}>{children}</div>
    </div>
  );
}
