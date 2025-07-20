"use client";

import { useState, useEffect } from "react";
import Button from "@/src/components/common/Button";
import { Search } from "lucide-react";
import OrderDetailModal from "../order/OrderDetailModal";
import { useOrders } from "@/src/store/order";
import { Order } from "@/src/types";

function formatOrderSummary(
  items: { name?: string; count: number; productId: number }[]
) {
  if (items.length === 0) return "-";
  const firstItem = items[0];
  const extraCount = items.length - 1;
  return extraCount > 0
    ? `${firstItem.name ?? `상품${firstItem.productId}`} 외 ${extraCount}건`
    : `${firstItem.name ?? `상품${firstItem.productId}`}`;
}

function getTotalPrice(items: { count: number; price: number }[]) {
  return items.reduce((sum, item) => sum + item.count * item.price, 0);
}

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

export default function OrdersPanel() {
  const { orders, loading, error, fetchOrderDetail } = useOrders();

  console.log("OrdersPanel - 주문 내역:", orders.length, "개");
  console.log("OrdersPanel - 로딩 상태:", loading);
  console.log("OrdersPanel - 에러:", error);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isLoadingDetail, setIsLoadingDetail] = useState(false);

  const openModal = async (order: Order) => {
    setIsLoadingDetail(true);
    try {
      // 서버에서 최신 상세 정보 조회
      const freshOrderDetail = await fetchOrderDetail(order.id);
      setSelectedOrder(freshOrderDetail);
      setIsModalOpen(true);
    } catch (error) {
      console.error("주문 상세 정보 조회 실패:", error);
      // 실패 시 기존 주문 정보로 대체
      setSelectedOrder(order);
      setIsModalOpen(true);
    } finally {
      setIsLoadingDetail(false);
    }
  };

  return (
    <section className="bg-white shadow p-6 rounded">
      {/* 헤더 */}
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold">주문 내역</h2>
        <Button icon={Search} text="더미 버튼" className="invisible" />
      </div>

      {/* 주문 목록 */}
      {orders.length > 0 ? (
        <table className="w-full text-lg border-t border-gray-200">
          <thead className="bg-gray-50">
            <tr className="text-center">
              <th className="p-3 text-gray-500 font-medium">주문번호</th>
              <th className="p-3 text-gray-500 font-medium">주문일</th>
              <th className="p-3 text-gray-500 font-medium">상태</th>
              <th className="p-3 text-gray-500 font-medium">주문내역</th>
              <th className="p-3 text-gray-500 font-medium">결제금액</th>
              <th className="p-3 text-gray-500 font-medium"></th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr
                key={order.id}
                className="border-b border-gray-100 hover:bg-gray-50 text-center"
              >
                <td className="p-3 text-gray-600">{order.id}</td>
                <td className="p-3 text-gray-900">
                  {formatDate(order.createdDate)}
                </td>
                <td className="p-3 text-gray-900">
                  {formatOrderStatus(order.state)}
                </td>
                <td className="p-3 text-gray-900">
                  {formatOrderSummary(order.orderItems)}
                </td>
                {/* 결제금액 */}
                <td className="p-3 text-gray-900 font-semibold">
                  {getTotalPrice(order.orderItems).toLocaleString()}원
                </td>
                <td className="p-3 text-right">
                  <Button
                    text={isLoadingDetail ? "로딩..." : "상세보기"}
                    onClick={() => openModal(order)}
                    className="text-sm"
                    disabled={isLoadingDetail}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <div className="text-center py-8">
          <p className="text-gray-500">주문 내역이 없습니다.</p>
        </div>
      )}

      {/* 주문 상세 모달 */}
      {isModalOpen && selectedOrder && (
        <OrderDetailModal
          order={selectedOrder}
          onClose={() => setIsModalOpen(false)}
        />
      )}
    </section>
  );
}
