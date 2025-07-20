"use client";

import { useEffect, useState } from "react";
import {
  Coffee,
  LogIn,
  LogOut,
  Shield,
  ShoppingCart,
  User,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { useUser } from "@/src/store/auth";
import Button from "@/src/components/common/Button";
import CartSidebar from "@/src/components/features/cart/CartSidebar";
import CompleteModal from "@/src/components/features/order/CompleteModal";
import { useCart } from "@/src/store/cart";

// 상단 네비게이션/헤더 컴포넌트
// 인증 상태에 따라 로그인/로그아웃/마이페이지/관리자/장바구니 버튼 표시
// UserContext, CartContext 등 전역 상태 사용
const Header = () => {
  // 클라이언트에서만 렌더링되도록 마운트 상태 관리
  const [isMounted, setIsMounted] = useState(false);
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  useEffect(() => {
    setIsMounted(true);
  }, []);

  // 인증 상태/유저 정보
  const { user, isAuthenticated, clearUser } = useUser();
  // 장바구니 상태/핸들러
  const { isCartOpen, toggleCart, cartItems, setCartItems } = useCart();
  const router = useRouter();

  // 장바구니 전체 수량 계산
  const totalQuantity = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  // 로그아웃 핸들러
  const handleLogout = () => {
    clearUser();
    // 즉시 홈으로 이동하여 AuthGuard 메시지 방지
    router.push("/");
    setCompleteMessage("로그아웃되었습니다.");
    setCompleteOpen(true);
  };

  return (
    <>
      <header className="bg-white p-5 border-b border-gray-300 shadow-md">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center">
            {/* 로고/홈 이동 */}
            <div
              className="flex items-center space-x-4 cursor-pointer"
              onClick={() => router.push("/")}
            >
              <Coffee className="h-10 w-10 text-amber-600" />
              <div className="leading-tight">
                <h1 className="text-[20px] font-semibold text-gray-800 leading-none">
                  우아한 카페
                </h1>
                <p className="text-sm text-gray-500 leading-none mt-2">
                  우아한 커피와 디저트
                </p>
              </div>
            </div>

            <div className="ml-auto">
              {/* 인증 상태별 버튼 렌더링 */}
              {isMounted &&
                (!isAuthenticated ? (
                  // 로그인 안 된 경우
                  <Button
                    icon={LogIn}
                    text="로그인"
                    onClick={() => router.push("/member/login")}
                  />
                ) : (
                  // 로그인 된 경우
                  <div className="flex items-center space-x-4">
                    {/* 관리자 계정이면 관리자 페이지 버튼 */}
                    {user?.isAdmin && (
                      <Button
                        icon={Shield}
                        text="관리자 페이지"
                        onClick={() => router.push("/admin")}
                      />
                    )}

                    {/* 마이페이지 이동 */}
                    <Button
                      icon={User}
                      text="마이 페이지"
                      onClick={() => router.push("/member/mypage")}
                    />
                    {/* 장바구니 버튼 및 수량 표시 */}
                    <div className="relative">
                      <Button
                        icon={ShoppingCart}
                        text="장바구니"
                        onClick={toggleCart}
                      />
                      {totalQuantity > 0 && (
                        <span className="absolute -top-2 -right-2 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs text-white">
                          {totalQuantity}
                        </span>
                      )}
                    </div>
                    {/* 로그아웃 버튼 */}
                    <Button
                      icon={LogOut}
                      text="로그아웃"
                      onClick={handleLogout}
                    />
                  </div>
                ))}
            </div>
          </div>
        </div>
      </header>
      {/* 장바구니 사이드바 */}
      {isMounted && (
        <CartSidebar
          isOpen={isCartOpen}
          onClose={toggleCart}
          cartItems={cartItems}
          setCartItems={setCartItems}
        />
      )}
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={() => {
          setCompleteOpen(false);
        }}
        message={completeMessage}
      />
    </>
  );
};

export default Header;
