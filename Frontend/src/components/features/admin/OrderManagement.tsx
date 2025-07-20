"use client";

import React, { useState, useEffect, useCallback } from "react";
import type { components } from "@/src/lib/backend/api/schema.d.ts";
type AdminOrder = components["schemas"]["OrderDtoWithName"];
type OrderStatus = AdminOrder["state"];
import Button from "@/src/components/common/Button";
import {
  FaPhone,
  FaMapMarkerAlt,
  FaChevronDown,
  FaChevronUp,
} from "react-icons/fa";
import { useAdminOrders } from "@/src/store/order/AdminOrderContext";

const OrderManagement: React.FC = () => {
  // fetchAdminOrders를 사용하여 관리자 주문 목록을 불러온다
  const { orders, fetchAdminOrders, loading, error, updateOrderStatus } =
    useAdminOrders();
  const [changedOrders, setChangedOrders] = useState<Set<number>>(new Set());
  const [filteredOrders, setFilteredOrders] = useState<AdminOrder[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "All">("All");
  const [expandedOrders, setExpandedOrders] = useState<Set<number>>(new Set());

  // 컴포넌트 마운트 시 관리자 주문 목록 불러오기
  useEffect(() => {
    fetchAdminOrders();
  }, [fetchAdminOrders]);

  useEffect(() => {
    let updatedOrders = orders;

    if (searchTerm) {
      updatedOrders = updatedOrders.filter(
        (order) =>
          order.customerEmail
            .toLowerCase()
            .includes(searchTerm.toLowerCase()) ||
          order.id.toString().includes(searchTerm.toLowerCase())
      );
    }

    if (statusFilter !== "All") {
      updatedOrders = updatedOrders.filter(
        (order) => order.state === statusFilter
      );
    }

    setFilteredOrders(updatedOrders);
  }, [searchTerm, statusFilter, orders]);

  const handleStatusChange = (orderId: number, newStatus: OrderStatus) => {
    setFilteredOrders((prevOrders) =>
      prevOrders.map((order) =>
        order.id === orderId ? { ...order, state: newStatus } : order
      )
    );
    setChangedOrders((prev) => new Set(prev.add(orderId)));
  };

  const handleSave = async () => {
    try {
      for (const orderId of changedOrders) {
        const orderToUpdate = filteredOrders.find(
          (order) => order.id === orderId
        );
        if (orderToUpdate) {
          await updateOrderStatus(orderToUpdate.id, orderToUpdate.state);
        }
      }
      alert("변경사항이 저장되었습니다.");
      setChangedOrders(new Set());
      // fetchAdminOrders는 updateOrderStatus에서 이미 호출됨
    } catch (err) {
      console.error("Failed to save order changes:", err);
      alert("변경사항 저장에 실패했습니다.");
    }
  };

  const hasChanges = changedOrders.size > 0;

  const statusOptions: { value: OrderStatus | "All"; label: string }[] = [
    { value: "All", label: "전체" },
    { value: "ORDERED", label: "주문완료" },
    { value: "SHIPPING", label: "배송중" },
    { value: "COMPLETED", label: "배송완료" },
    { value: "CANCELED", label: "주문취소" },
  ];

  const getStatusBadgeColor = (status: OrderStatus) => {
    switch (status) {
      case "ORDERED":
        return "bg-blue-100 text-blue-800";
      case "PAID":
        return "bg-yellow-100 text-yellow-800";
      case "SHIPPING":
        return "bg-purple-100 text-purple-800";
      case "COMPLETED":
        return "bg-green-100 text-green-800";
      case "CANCELED":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const toggleExpandOrder = (orderId: number) => {
    setExpandedOrders((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(orderId)) {
        newSet.delete(orderId);
      } else {
        newSet.add(orderId);
      }
      return newSet;
    });
  };

  return (
    <div className="container mx-auto p-4 sm:p-6 lg:p-8 min-h-screen">
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-gray-800">주문 관리</h1>
        <p className="text-gray-500 mt-2">
          주문 내역을 확인하고 상태를 변경하세요.
        </p>
      </div>

      <div className="mb-6 flex flex-col sm:flex-row justify-between items-center gap-4">
        <div className="flex flex-col sm:flex-row items-center gap-4 w-full sm:w-auto">
          <input
            type="text"
            placeholder="주문번호, 이름 검색..."
            className="p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 w-full sm:w-64"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <select
            className="p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 w-full sm:w-auto"
            value={statusFilter}
            onChange={(e) =>
              setStatusFilter(e.target.value as OrderStatus | "All")
            }
          >
            {statusOptions.map((status) => (
              <option key={status.value} value={status.value}>
                {status.label}
              </option>
            ))}
          </select>
        </div>
        <Button
          onClick={handleSave}
          disabled={!hasChanges}
          className="w-full sm:w-auto"
          text="변경사항 저장"
          fontColor="text-white"
          bgColor="bg-blue-600"
          hoverColor="hover:bg-blue-700"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredOrders.map((order) => {
          const isExpanded = expandedOrders.has(order.id);
          const itemsToShow = isExpanded
            ? order.orderItems ?? []
            : (order.orderItems ?? []).slice(0, 2);

          return (
            <div
              key={order.id}
              className={`bg-white rounded-lg shadow-md p-6 transition-all duration-300 hover:shadow-xl flex flex-col ${
                changedOrders.has(order.id) ? "ring-2 ring-yellow-400" : ""
              }`}
            >
              <div className="flex justify-between items-start mb-4">
                <div>
                  <div className="text-lg font-bold text-gray-800">
                    #{order.id}
                  </div>
                  <div className="text-sm text-gray-500">
                    {new Date(order.createdDate).toLocaleDateString()}
                  </div>
                </div>
                <div
                  className={`text-xs font-bold py-1 px-3 rounded-full ${getStatusBadgeColor(
                    order.state
                  )}`}
                >
                  {statusOptions.find((option) => option.value === order.state)
                    ?.label || order.state}
                </div>
              </div>

              <div className="mb-4">
                <div className="text-lg font-semibold text-gray-800">
                  {order.customerEmail}
                </div>
                <div className="flex items-center text-sm text-gray-600 mt-1">
                  <FaMapMarkerAlt className="mr-2" />
                  {order.customerAddress}
                </div>
              </div>

              <div className="mb-4">
                <h4 className="font-semibold text-gray-700 mb-2">주문 상품</h4>
                <ul className="space-y-2">
                  {itemsToShow.map((item, index) => (
                    <li key={index} className="flex justify-between text-sm">
                      <span>
                        {item.productName
                          ? `${item.productName}`
                          : `상품 ID: ${item.productId}`}{" "}
                        x {item.count}
                      </span>
                      <span>
                        {(item.price * item.count).toLocaleString()}원
                      </span>
                    </li>
                  ))}
                </ul>
                {order.orderItems?.length > 2 && (
                  <button
                    onClick={() => toggleExpandOrder(order.id)}
                    className="text-sm text-blue-500 hover:underline mt-2 flex items-center"
                  >
                    {isExpanded ? "접기" : "더보기"}
                    {isExpanded ? (
                      <FaChevronUp className="ml-1" />
                    ) : (
                      <FaChevronDown className="ml-1" />
                    )}
                  </button>
                )}
              </div>

              <div className="border-t border-gray-200 pt-4 mt-auto">
                <div className="flex justify-between items-center mb-4">
                  <span className="font-bold text-gray-800">총 가격</span>
                  <span className="font-bold text-xl text-blue-600">
                    {order.orderItems
                      .reduce((sum, item) => sum + item.price * item.count, 0)
                      .toLocaleString()}
                    원
                  </span>
                </div>
                <select
                  value={order.state}
                  onChange={(e) =>
                    handleStatusChange(order.id, e.target.value as OrderStatus)
                  }
                  className="w-full p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {statusOptions.slice(1).map((status) => (
                    <option key={status.value} value={status.value}>
                      {status.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default OrderManagement;
