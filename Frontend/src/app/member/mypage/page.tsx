"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import MypageTabs from "@/src/components/features/mypage/MypageTabs";
import UserInfoPanel from "@/src/components/features/mypage/UserInfoPanel";
import AddressPanel from "@/src/components/features/mypage/AddressPanel";
import OrdersPanel from "@/src/components/features/mypage/OrdersPanel";
import SettingsPanel from "@/src/components/features/mypage/SettingsPanel";
import { useUser } from "@/src/store/auth";
import { useAddressContext } from "@/src/store/address";
import { useOrders } from "@/src/store/order";
import { AuthGuard } from "@/src/components/common/AuthGuard";

export default function Mypage() {
  const [activeTab, setActiveTab] = useState<
    "info" | "address" | "orders" | "settings"
  >("info");

  const { user } = useUser();
  const { fetchAddresses } = useAddressContext();
  const { fetchOrders } = useOrders();

  // 마이페이지 진입 시 주소 목록과 주문 내역 새로 로드
  useEffect(() => {
    if (user) {
      fetchAddresses();
      fetchOrders();
    }
  }, [user, fetchAddresses, fetchOrders]);

  return (
    <AuthGuard requireAuth={true}>
      <main className="max-w-7xl mx-auto py-12 px-4">
        <h1 className="text-2xl font-semibold mb-6">마이 페이지</h1>
        <MypageTabs activeTab={activeTab} setActiveTab={setActiveTab} />

        <div className="mt-8">
          {activeTab === "info" && <UserInfoPanel />}
          {activeTab === "address" && <AddressPanel />}
          {activeTab === "orders" && <OrdersPanel />}
          {activeTab === "settings" && <SettingsPanel />}
        </div>
      </main>
    </AuthGuard>
  );
}
