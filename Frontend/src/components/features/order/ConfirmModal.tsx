"use client";

import { Modal } from "@/src/components/common/Modal";
import { ModalContent } from "@/src/components/common/ModalContent";
import Button from "@/src/components/common/Button";

interface ConfirmModalProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  message: string;
}

export default function ConfirmModal({
  open,
  onClose,
  onConfirm,
  message,
}: ConfirmModalProps) {
  if (!open) return null;

  return (
    <Modal onClose={onClose}>
      <ModalContent>
        <div className="space-y-6">
          {/* 제목 */}
          <h3 className="text-xl font-bold text-gray-900">확인</h3>

          {/* 메시지 */}
          <p className="text-base text-gray-700 whitespace-pre-line">
            {message}
          </p>

          {/* 버튼 그룹 */}
          <div className="flex gap-3">
            <Button
              text="취소"
              onClick={onClose}
              className="flex-1 px-5 py-3 text-sm border"
              bgColor="bg-white"
              fontColor="text-gray-700"
              hoverColor="hover:bg-gray-100"
            />
            <Button
              text="확인"
              onClick={onConfirm}
              className="flex-1 px-5 py-3 text-sm border"
              bgColor="bg-amber-600"
              fontColor="text-white"
              hoverColor="hover:bg-amber-500"
            />
          </div>
        </div>
      </ModalContent>
    </Modal>
  );
}
