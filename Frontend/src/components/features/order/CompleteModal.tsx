"use client";

import { Modal } from "@/src/components/common/Modal";
import { ModalContent } from "@/src/components/common/ModalContent";
import Button from "@/src/components/common/Button";

interface OrderCompleteModalProps {
  open: boolean;
  onClose: () => void;
  message?: string;
}

export default function OrderCompleteModal({
  open,
  onClose,
  message,
}: OrderCompleteModalProps) {
  if (!open) return null;

  return (
    <Modal onClose={onClose}>
      <ModalContent>
        <div className="space-y-6">
          {/* 제목 */}
          <h3 className="text-xl font-bold text-gray-900">알림</h3>

          {/* 메시지 */}
          <p className="text-base text-gray-700 whitespace-pre-line">
            {message}
          </p>

          {/* 버튼 */}
          <div className="flex justify-end">
            <Button
              text="확인"
              onClick={onClose}
              className="px-5 py-3 text-sm border"
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
