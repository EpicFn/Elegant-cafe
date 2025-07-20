"use client";

import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useCallback,
  useEffect,
} from "react";
import { useUser } from "@/src/store/auth";
import { AddressService } from "@/src/services/addressService";

export interface Address {
  id: number;
  content: string;
  isDefault?: boolean;
}

// AddressContext에서 제공하는 값/함수 타입
interface AddressContextType {
  addresses: Address[];
  add: (address: string) => Promise<void>;
  edit: (id: number, newAddress: string) => Promise<void>;
  remove: (id: number) => Promise<void>;
  setDefault: (id: number) => Promise<void>;
  reset: () => void;
  fetchAddresses: () => Promise<void>;
}

const AddressContext = createContext<AddressContextType | undefined>(undefined);

export function AddressProvider({ children }: { children: ReactNode }) {
  const { user } = useUser();
  const [addresses, setAddresses] = useState<Address[]>([]);

  // 주소 목록 조회
  const fetchAddresses = useCallback(async () => {
    try {
      const addressList = await AddressService.getAddressList();
      setAddresses(addressList);
    } catch (error) {
      console.error("주소 목록 조회 실패:", error);
      setAddresses([]);
    }
  }, []);

  // user 정보가 있을 때만 주소 목록 fetch
  useEffect(() => {
    if (user) {
      fetchAddresses();
    } else {
      setAddresses([]); // 로그아웃/탈퇴 시 주소 초기화
    }
  }, [user, fetchAddresses]);

  // 주소 등록
  const add = useCallback(async (address: string) => {
    try {
      const newAddress = await AddressService.submitAddress(address);
      setAddresses((prev) => [
        ...prev,
        {
          id: newAddress.id,
          content: newAddress.content,
          isDefault: false,
        },
      ]);
    } catch (error) {
      console.error("주소 등록 실패:", error);
      throw error;
    }
  }, []);

  // 주소 수정
  const edit = useCallback(async (id: number, newAddress: string) => {
    try {
      const updatedAddress = await AddressService.updateAddress(id, newAddress);
      // 로컬 상태 직접 업데이트
      setAddresses((prev) =>
        prev.map((addr) =>
          addr.id === id ? { ...addr, content: updatedAddress.content } : addr
        )
      );
    } catch (error) {
      console.error("주소 수정 실패:", error);
      throw error;
    }
  }, []);

  // 주소 삭제
  const remove = useCallback(async (id: number) => {
    try {
      await AddressService.deleteAddress(id);
      setAddresses((prev) => prev.filter((a) => a.id !== id));
    } catch (error) {
      console.error("주소 삭제 실패:", error);
      throw error;
    }
  }, []);

  // 기본 주소 설정
  const setDefault = useCallback(async (id: number) => {
    try {
      await AddressService.setDefaultAddress(id);
      // 로컬 상태 직접 업데이트
      setAddresses((prev) =>
        prev.map((addr) => ({
          ...addr,
          isDefault: addr.id === id,
        }))
      );
    } catch (error) {
      console.error("기본 주소 설정 실패:", error);
      throw error;
    }
  }, []);

  // 주소 전체 초기화
  const reset = useCallback(() => setAddresses([]), []);

  return (
    <AddressContext.Provider
      value={{
        addresses,
        add,
        edit,
        remove,
        setDefault,
        reset,
        fetchAddresses,
      }}
    >
      {children}
    </AddressContext.Provider>
  );
}

// AddressContext 사용 훅
export function useAddressContext() {
  const context = useContext(AddressContext);
  if (context === undefined) {
    throw new Error("useAddressContext must be used within an AddressProvider");
  }
  return context;
}
