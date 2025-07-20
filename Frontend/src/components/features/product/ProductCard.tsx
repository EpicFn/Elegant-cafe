"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Button from "@/src/components/common/Button";
import { Product } from "@/src/types";
import { useUser } from "@/src/store/auth";
import CompleteModal from "@/src/components/features/order/CompleteModal";

interface ProductCardProps {
  product: Product;
  addToCart: (product: Product, quantity: number) => void;
}

const ProductCard = ({ product, addToCart }: ProductCardProps) => {
  const [quantity, setQuantity] = useState(0);
  const { isAuthenticated } = useUser();
  const router = useRouter();
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  const orderable = product.orderable;

  const handleIncrease = () => {
    if (orderable) {
      setQuantity(quantity + 1);
    }
  };

  const handleDecrease = () => {
    if (orderable && quantity > 0) {
      setQuantity(quantity - 1);
    }
  };

  const handleAddToCart = () => {
    if (!orderable) return;

    if (!isAuthenticated) {
      setCompleteMessage("로그인이 필요한 서비스입니다.");
      setCompleteOpen(true);
      return;
    }

    const quantityToAdd = quantity === 0 ? 1 : quantity;
    addToCart(product, quantityToAdd);
    setQuantity(0);
  };

  return (
    <>
      <div
        className={`group relative flex h-full w-full flex-col overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-all duration-300 hover:shadow-lg`}
      >
        <div className="aspect-w-3 aspect-h-4 overflow-hidden rounded-t-lg bg-gray-200 relative">
          <img
            src={product.imageUrl || ""}
            alt={product.productName}
            className={`h-full w-full object-cover object-center transition-all duration-300 group-hover:scale-105 ${
              !orderable ? "grayscale" : ""
            }`}
          />
          {!orderable && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/50 bg-opacity-40 z-10">
              <span className="text-white text-2xl font-bold tracking-wider">
                SOLD OUT
              </span>
            </div>
          )}
        </div>
        <div className="flex flex-1 flex-col p-4">
          <div className="flex justify-between items-center mb-2">
            <h3
              className={`text-base font-semibold ${
                !orderable ? "text-gray-500" : "text-gray-900"
              }`}
            >
              {product.productName}
            </h3>
            <p
              className={`text-xs px-2 py-1 rounded-full ${
                !orderable
                  ? "bg-gray-300 text-gray-600"
                  : "bg-amber-100 text-amber-800"
              }`}
            >
              {product.category}
            </p>
          </div>
          <div className="flex flex-1 flex-col">
            <p
              className={`text-sm mb-3 ${
                !orderable ? "text-gray-400" : "text-gray-500"
              } flex-grow`}
            >
              {product.description}
            </p>
            <p
              className={`text-xl font-bold mt-auto ${
                !orderable ? "text-gray-500" : "text-amber-700"
              }`}
            >
              {product.price.toLocaleString()}원
            </p>
          </div>
          <div className="flex flex-col justify-end pt-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center rounded-md border border-gray-300">
                <button
                  onClick={handleDecrease}
                  className={`px-3 py-1 text-gray-600 transition hover:bg-gray-100 rounded-l-md ${
                    !orderable ? "cursor-not-allowed" : "cursor-pointer"
                  }`}
                  disabled={!orderable}
                >
                  -
                </button>
                <span className="px-4 py-1 text-sm font-medium text-gray-800">
                  {quantity}
                </span>
                <button
                  onClick={handleIncrease}
                  className={`px-3 py-1 text-gray-600 transition hover:bg-gray-100 rounded-r-md ${
                    !orderable ? "cursor-not-allowed" : "cursor-pointer"
                  }`}
                  disabled={!orderable}
                >
                  +
                </button>
              </div>
              <Button
                text="+ 담기"
                bgColor={!orderable ? "bg-gray-400" : "bg-black"}
                fontColor="text-white"
                hoverColor={
                  !orderable ? "hover:bg-gray-400" : "hover:bg-gray-800"
                }
                className={`text-sm ${!orderable ? "cursor-not-allowed" : ""}`}
                onClick={handleAddToCart}
                disabled={!orderable}
              />
            </div>
          </div>
        </div>
      </div>
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={() => {
          setCompleteOpen(false);
          if (completeMessage === "로그인이 필요한 서비스입니다.") {
            router.push("/member/login");
          }
        }}
        message={completeMessage}
      />
    </>
  );
};

export default ProductCard;
