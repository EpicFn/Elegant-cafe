"use client";

import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useEffect,
} from "react";
import { Product, CartItem } from "@/src/types";

// CartContext: 장바구니 전역 상태 관리 컨텍스트
// - 장바구니 아이템 목록, 추가/삭제/수량변경, 열기/닫기 등 상태 및 함수 제공
interface CartContextType {
  cartItems: CartItem[];
  isCartOpen: boolean;
  addToCart: (product: Product, quantity: number) => void;
  toggleCart: () => void;
  setCartItems: React.Dispatch<React.SetStateAction<CartItem[]>>;
  clearCart: () => void;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

// useCart: CartContext의 값을 반환하는 커스텀 훅
export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
};

// CartProvider: 장바구니 전역 상태를 앱 전체에 제공하는 Provider 컴포넌트
// - cartItems(장바구니 목록), isCartOpen(사이드바 열림 여부) 등 상태 관리
export const CartProvider = ({ children }: { children: ReactNode }) => {
  const [cartItems, setCartItems] = useState<CartItem[]>(() => {
    if (typeof window !== "undefined") {
      const savedCartItems = localStorage.getItem("cartItems");
      return savedCartItems ? JSON.parse(savedCartItems) : [];
    }
    return [];
  });
  const [isCartOpen, setIsCartOpen] = useState(false);

  // cartItems가 변경될 때마다 localStorage에 저장 (새로고침/재방문 시 복원)
  useEffect(() => {
    if (typeof window !== "undefined") {
      localStorage.setItem("cartItems", JSON.stringify(cartItems));
    }
  }, [cartItems]);

  // addToCart: 상품을 장바구니에 추가하는 함수
  // - 이미 담긴 상품이면 수량만 증가, 없으면 새로 추가
  const addToCart = (product: Product, quantity: number) => {
    setCartItems((prevItems) => {
      const existingItem = prevItems.find(
        (item) => item.product.id === product.id
      );
      if (existingItem) {
        return prevItems.map((item) =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        return [...prevItems, { product, quantity }];
      }
    });
  };

  // toggleCart: 장바구니 사이드바의 열림/닫힘 상태를 토글하는 함수
  const toggleCart = () => {
    setIsCartOpen(!isCartOpen);
  };

  // clearCart: 장바구니를 완전히 비우는 함수
  const clearCart = () => {
    setCartItems([]);
  };

  return (
    <CartContext.Provider
      value={{
        cartItems,
        isCartOpen,
        addToCart,
        toggleCart,
        setCartItems,
        clearCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};
