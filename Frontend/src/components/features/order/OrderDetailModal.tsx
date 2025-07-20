"use client";

import { Modal } from "@/src/components/common/Modal";
import { ModalContent } from "@/src/components/common/ModalContent";
import Button from "@/src/components/common/Button";
import { useState } from "react";
import AddressSelectModal from "@/src/components/features/address/AddressSelectModal";
import ConfirmModal from "@/src/components/features/order/ConfirmModal";
import CompleteModal from "@/src/components/features/order/CompleteModal";
import { useOrders } from "@/src/store/order";
import { Order } from "@/src/types";

interface OrderDetailModalProps {
  order: Order;
  onClose: () => void;
}

export default function OrderDetailModal({
  order,
  onClose,
}: OrderDetailModalProps) {
  const { cancelOrder, updateOrderAddress } = useOrders();
  const totalPrice = order.orderItems.reduce(
    (sum, item) => sum + item.count * item.price,
    0
  );
  const canModify = order.state === "ORDERED";
  const [selectedAddress, setSelectedAddress] = useState(order.customerAddress);
  const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
  const [confirmModal, setConfirmModal] = useState({
    open: false,
    message: "",
    onConfirm: () => {},
  });
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  const handleCancelOrder = () => {
    setConfirmModal({
      open: true,
      message: "주문을 취소하시겠습니까?",
      onConfirm: async () => {
        try {
          await cancelOrder(order.id);
          setConfirmModal({ ...confirmModal, open: false });
          setCompleteMessage("주문이 취소되었습니다.");
          setCompleteOpen(true);
        } catch (error) {
          console.error("주문 취소 실패:", error);
          setCompleteMessage("주문 취소에 실패했습니다. 다시 시도해 주세요.");
          setCompleteOpen(true);
        }
      },
    });
  };

  function formatDate(dateString: string) {
    return new Date(dateString).toLocaleDateString("ko-KR");
  }

  function formatOrderStatus(status: string) {
    const statusMap: Record<string, string> = {
      ORDERED: "주문완료",
      SHIPPING: "배송중",
      COMPLETED: "배송완료",
      CANCELED: "주문취소",
    };
    return statusMap[status] || status;
  }

  return (
    <Modal onClose={onClose} size="large">
      <ModalContent size="large" className="p-10">
        <div className="min-h-[500px] flex flex-col md:flex-row md:gap-10 gap-10">
          {/* 좌측: 주문 정보 + 상품 목록 */}
          <div className="flex-2 flex flex-col space-y-6">
            {/* 주문 정보 */}
            <section className="border rounded-lg p-6 bg-white shadow-sm">
              <h2 className="text-2xl font-bold mb-4">주문 상세</h2>
              <dl className="text-lg space-y-2">
                <div className="flex justify-between">
                  <dt className="text-gray-500">주문번호</dt>
                  <dd>{order.id}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-gray-500">주문일</dt>
                  <dd>{formatDate(order.createdDate)}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-gray-500">상태</dt>
                  <dd className="text-orange-600 font-semibold">
                    {formatOrderStatus(order.state)}
                  </dd>
                </div>
              </dl>
            </section>

            {/* 주문 상품 */}
            <section className="border rounded-lg p-6 bg-white shadow-sm flex-1">
              <h3 className="text-xl font-semibold mb-4">주문 상품 목록</h3>
              <ul className="space-y-4">
                {order.orderItems.map((item, idx) => (
                  <li
                    key={item.id}
                    className="text-lg border-b pb-3 last:border-b-0"
                  >
                    <div className="flex justify-between items-center">
                      <span>
                        {item.name ?? `상품${item.productId}`} × {item.count}
                      </span>
                      <span className="font-semibold">
                        {(item.count * item.price).toLocaleString()}원
                      </span>
                    </div>
                    <p className="text-gray-500 text-base mt-1">
                      단가: {item.price.toLocaleString()}원
                    </p>
                  </li>
                ))}
              </ul>
            </section>
          </div>

          {/* 우측: 배송지 + 금액 + 버튼 */}
          <div className="w-full md:w-[280px] flex-1 flex-col justify-between gap-8">
            {/* 배송지 + 금액 */}
            <section className="border rounded-lg p-6 bg-gray-50 shadow-sm space-y-6">
              {/* 배송지 정보 */}
              <div className="space-y-2">
                <h3 className="text-xl font-semibold">배송지 정보</h3>
                <p className="text-lg text-gray-800">{selectedAddress}</p>
                <Button
                  text="배송지 변경"
                  onClick={
                    canModify ? () => setIsAddressModalOpen(true) : undefined
                  }
                  bgColor={canModify ? undefined : "bg-gray-200"}
                  hoverColor={canModify ? undefined : ""}
                  fontColor={canModify ? undefined : "text-gray-400"}
                  className="w-full mt-2 text-lg"
                />

                {/* 배송지 변경 모달 */}
                <AddressSelectModal
                  open={isAddressModalOpen}
                  onClose={() => setIsAddressModalOpen(false)}
                  currentAddress={selectedAddress}
                  onEditSave={async (edited: string) => {
                    await updateOrderAddress(order.id, edited);
                    setSelectedAddress(edited);
                    setCompleteMessage("배송지가 변경되었습니다.");
                    setCompleteOpen(true);
                  }}
                  onSelect={async (address: string) => {
                    await updateOrderAddress(order.id, address);
                    setSelectedAddress(address);
                    setIsAddressModalOpen(false);
                    setCompleteMessage("배송지가 변경되었습니다.");
                    setCompleteOpen(true);
                  }}
                />
              </div>

              {/* 총 결제 금액 */}
              <div className="border-t pt-4">
                <h3 className="text-xl font-semibold">총 결제 금액</h3>
                <p className="text-2xl text-orange-600 text-right font-bold">
                  {totalPrice.toLocaleString()}원
                </p>
              </div>
            </section>

            {/* 버튼 */}
            <div className="border-t pt-6 space-y-4">
              <Button
                text="주문 취소"
                onClick={canModify ? handleCancelOrder : undefined}
                bgColor={canModify ? "bg-amber-600" : "bg-gray-200"}
                hoverColor={canModify ? "hover:bg-amber-700" : ""}
                fontColor={canModify ? "text-white" : "text-gray-400"}
                className="w-full text-lg py-3 rounded font-semibold transition cursor-default"
              />
              <Button
                text="닫기"
                onClick={onClose}
                className="w-full text-lg py-3"
              />
            </div>
          </div>
        </div>
      </ModalContent>

      {/* 확인 모달 */}
      {confirmModal.open && (
        <ConfirmModal
          open={confirmModal.open}
          onClose={() => setConfirmModal({ ...confirmModal, open: false })}
          onConfirm={confirmModal.onConfirm}
          message={confirmModal.message}
        />
      )}
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={() => {
          setCompleteOpen(false);
          if (completeMessage === "주문이 취소되었습니다.") {
            onClose();
          }
        }}
        message={completeMessage}
      />
    </Modal>
  );
}
