"use client";

import { X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { PrimaryButton } from "@/src/components/common/PrimaryButton";
import { CartItem } from "@/src/types";
import { useAddressContext } from "@/src/store/address";
import { useCart } from "@/src/store/cart";
import { useOrders } from "@/src/store/order";
import { useUser } from "@/src/store/auth";
import AddressSelectModal from "@/src/components/features/address/AddressSelectModal";
import CompleteModal from "@/src/components/features/order/CompleteModal";

// CartSidebar: 장바구니 사이드바 UI 컴포넌트
// - 장바구니 열기/닫기, 장바구니 아이템 목록, 수량/삭제/결제 등 기능 제공
interface CartSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  cartItems: CartItem[];
  setCartItems: React.Dispatch<React.SetStateAction<CartItem[]>>;
}

const CartSidebar: React.FC<CartSidebarProps> = ({
  isOpen,
  onClose,
  cartItems,
  setCartItems,
}) => {
  const sidebarRef = useRef<HTMLDivElement>(null);
  const [isClient, setIsClient] = useState(false);
  useEffect(() => {
    setIsClient(true);
  }, []);
  const [selectedAddress, setSelectedAddress] = useState<string>("");
  const [isEditingAddress, setIsEditingAddress] = useState(false);
  const { createOrder } = useOrders();
  const { user } = useUser();
  const { clearCart } = useCart();
  const [ordering, setOrdering] = useState(false);
  const [orderComplete, setOrderComplete] = useState(false);
  const [errorModalOpen, setErrorModalOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  // handleRemoveItem: 장바구니에서 아이템 삭제
  const handleRemoveItem = (id: number) => {
    setCartItems((prevItems) =>
      prevItems.filter((item) => item.product.id !== id)
    );
  };

  // handleQuantityChange: 장바구니 아이템의 수량 변경
  const handleQuantityChange = (id: number, newQuantity: number) => {
    setCartItems((prevItems) =>
      prevItems.map((item) =>
        item.product.id === id
          ? { ...item, quantity: Math.max(1, newQuantity) }
          : item
      )
    );
  };

  // totalAmount: 장바구니 전체 금액 계산
  const totalAmount = cartItems.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );

  // useEffect: 외부 클릭 시 사이드바 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        sidebarRef.current &&
        !sidebarRef.current.contains(event.target as Node)
      ) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isOpen, onClose]);

  // handleOrder: 주문 처리 핸들러
  const handleOrder = async () => {
    if (!selectedAddress || cartItems.length === 0 || !user?.email) return;
    setOrdering(true);
    try {
      await createOrder({
        customerEmail: user.email,
        customerAddress: selectedAddress,
        orderItems: cartItems.map((item) => ({
          productId: item.product.id,
          count: item.quantity,
        })),
      });
      clearCart();
      setSelectedAddress("");
      setOrderComplete(true);
      setCompleteMessage("주문이 완료되었습니다.");
      setCompleteOpen(true);
    } catch (e) {
      setErrorMessage("주문 처리 중 오류가 발생했습니다.");
      setErrorModalOpen(true);
    } finally {
      setOrdering(false);
    }
  };

  if (!isClient) return null;

  return (
    <>
      {/* 오버레이: 사이드바가 열릴 때 배경을 어둡게 하고, 클릭 시 닫힘 */}
      {isOpen && (
        <div
          className="bg-black/50 fixed inset-0 z-40 transition-opacity duration-300"
          onClick={onClose}
        ></div>
      )}
      {/* 장바구니 사이드바 본체 */}
      <div
        ref={sidebarRef}
        className={`fixed top-0 right-0 h-full w-130 bg-white shadow-lg transform transition-transform duration-300 ease-in-out z-50 flex flex-col
          ${isOpen ? "translate-x-0" : "translate-x-full"}`}
      >
        {/* 헤더: 타이틀 + 닫기 버튼 */}
        <div className="flex justify-between items-center p-6 border-b">
          <h2 className="text-2xl font-bold">장바구니</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-800 cursor-pointer transition-colors duration-200"
          >
            <X size={28} />
          </button>
        </div>
        {/* 장바구니 아이템 목록 영역 */}
        <div className="flex-1 overflow-y-auto p-6">
          {cartItems.length === 0 ? (
            // 장바구니가 비어있을 때 안내 문구
            <p className="text-gray-500 text-center py-8">
              장바구니가 비어있습니다.
            </p>
          ) : (
            // 장바구니에 담긴 상품 목록
            <ul className="space-y-4">
              {cartItems.map((item) => (
                <li
                  key={item.product.id}
                  className={`flex items-center p-3 bg-white rounded-lg shadow-sm border border-gray-200 ${
                    !item.product.orderable ? "opacity-50 grayscale" : ""
                  }`}
                >
                  {/* 품절 상품 표시 오버레이 */}
                  {!item.product.orderable && (
                    <div className="absolute inset-0 flex items-center justify-center bg-gray-800 bg-opacity-75 z-10">
                      <span className="text-white text-xl font-bold">
                        SOLD OUT
                      </span>
                    </div>
                  )}
                  {/* 상품 이미지 */}
                  <img
                    src={item.product.imageUrl || ""}
                    alt={item.product.productName}
                    className="w-24 h-24 object-cover rounded-md mr-4 border border-gray-200"
                  />
                  {/* 상품 정보 및 수량 조절 */}
                  <div className="flex-1 flex flex-col justify-between">
                    <h3 className="font-semibold text-lg text-gray-800">
                      {item.product.productName}
                    </h3>
                    <p className="text-gray-600 text-sm">
                      {item.product.price.toLocaleString()}원
                    </p>
                    <div className="flex items-center mt-2">
                      {/* 수량 감소 버튼 */}
                      <button
                        onClick={() =>
                          handleQuantityChange(
                            item.product.id,
                            item.quantity - 1
                          )
                        }
                        className={`px-3 py-1 border border-gray-300 rounded-l-md bg-gray-50 hover:bg-gray-100 text-gray-700 transition-colors duration-200 ${
                          !item.product.orderable
                            ? "cursor-not-allowed"
                            : "cursor-pointer"
                        }`}
                        disabled={!item.product.orderable}
                      >
                        -
                      </button>
                      {/* 현재 수량 */}
                      <span className="px-4 py-1 border-t border-b border-gray-300 text-gray-800 font-medium">
                        {item.quantity}
                      </span>
                      {/* 수량 증가 버튼 */}
                      <button
                        onClick={() =>
                          handleQuantityChange(
                            item.product.id,
                            item.quantity + 1
                          )
                        }
                        className={`px-3 py-1 border border-gray-300 rounded-r-md bg-gray-50 hover:bg-gray-100 text-gray-700 transition-colors duration-200 ${
                          !item.product.orderable
                            ? "cursor-not-allowed"
                            : "cursor-pointer"
                        }`}
                        disabled={!item.product.orderable}
                      >
                        +
                      </button>
                    </div>
                  </div>
                  {/* 삭제 버튼 및 상품별 금액 */}
                  <div className="flex flex-col items-end justify-between h-full ml-4">
                    <button
                      onClick={() => handleRemoveItem(item.product.id)}
                      className={`text-red-500 hover:text-red-700 text-sm transition-colors duration-200 ${
                        !item.product.orderable
                          ? "cursor-not-allowed"
                          : "cursor-pointer"
                      }`}
                      disabled={!item.product.orderable}
                    >
                      삭제
                    </button>
                    <p className="text-gray-900 font-bold text-lg mt-auto">
                      {(item.product.price * item.quantity).toLocaleString()}원
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
        {/* 하단: 총 금액 및 주문 버튼 */}
        <div className="p-6 border-t border-gray-200">
          {/* 배송지 입력/선택 UI */}
          <div className="mb-6">
            <h3 className="text-lg font-semibold mb-2">배송지</h3>
            {!selectedAddress && !isEditingAddress ? (
              <div className="flex items-center gap-2 mb-2">
                <span className="text-base text-gray-500 flex-1">
                  배송지가 지정되어 있지 않습니다.
                </span>
                <button
                  className="px-3 py-1 text-sm bg-amber-100 text-amber-700 rounded hover:bg-amber-200 transition"
                  onClick={() => setIsEditingAddress(true)}
                >
                  배송지 지정
                </button>
              </div>
            ) : selectedAddress && !isEditingAddress ? (
              <div className="flex items-center gap-2 mb-2">
                <span className="text-base text-gray-800 flex-1">
                  {selectedAddress}
                </span>
                <button
                  className="px-3 py-1 text-sm bg-gray-200 rounded hover:bg-gray-300 transition"
                  onClick={() => setIsEditingAddress(true)}
                >
                  배송지 변경
                </button>
              </div>
            ) : null}

            {/* 배송지 변경 모달 */}
            <AddressSelectModal
              open={isEditingAddress}
              onClose={() => setIsEditingAddress(false)}
              currentAddress={selectedAddress}
              onEditSave={(edited: string) => {
                setSelectedAddress(edited);
              }}
              onSelect={(address) => setSelectedAddress(address)}
            />
          </div>
          <div className="flex justify-between items-center mb-5">
            <span className="text-xl font-semibold text-gray-800">
              총 금액:
            </span>
            <span className="text-2xl font-bold text-amber-600">
              {totalAmount.toLocaleString()}원
            </span>
          </div>
          {/* 주문하기 버튼 */}
          <PrimaryButton
            className="w-full py-3 text-lg font-semibold rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 disabled:bg-gray-300 disabled:text-gray-400 disabled:cursor-not-allowed"
            onClick={handleOrder}
            disabled={ordering || cartItems.length === 0 || !selectedAddress}
          >
            {ordering ? "주문 처리 중..." : "주문하기"}
          </PrimaryButton>
        </div>
      </div>
      {/* 주문 완료 모달 */}
      <CompleteModal
        open={orderComplete}
        onClose={() => setOrderComplete(false)}
        message="주문이 완료되었습니다."
      />
      {/* 에러 모달 */}
      <CompleteModal
        open={errorModalOpen}
        onClose={() => setErrorModalOpen(false)}
        message={errorMessage}
      />
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={() => setCompleteOpen(false)}
        message={completeMessage}
      />
    </>
  );
};

export default CartSidebar;
