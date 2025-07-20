import client from "@/src/lib/backend/client";
import type { components } from "@/src/lib/backend/api/schema.d.ts";
import type { OrderStatus } from "@/src/types/order";

// 타입 정의
type OrderDtoWithName = components["schemas"]["OrderDtoWithName"];
type OrderDtoWithSpecific = components["schemas"]["OrderDtoWithSpecific"];
type OrderItemDto = components["schemas"]["OrderItemDto"];

export interface Order {
  id: number;
  customerEmail: string;
  createdDate: string;
  state: "ORDERED" | "PAID" | "SHIPPING" | "COMPLETED" | "CANCELED";
  customerAddress: string;
  orderItems: {
    id: number;
    orderId: number;
    productId: number;
    count: number;
    price: number;
  }[];
}

export class AdminService {
  // 관리자용 주문 목록 조회
  static async getOrders(): Promise<OrderDtoWithName[]> {
    const { data: response, error } = await client.GET("/api/adm/orders");
    if (error) {
      throw new Error("주문 목록 조회에 실패했습니다.");
    }
    if (!response?.data) {
      return [];
    }
    // orderItems가 undefined면 []로, productName 등 필드 포함
    return response.data.map(order => ({
      ...order,
      orderItems: (order.orderItems ?? []).map(item => ({
        ...item,
        productName: item.productName ?? "",
      })),
    }));
  }

  // 주문 상세 조회 (관리자용)
  static async getOrderDetail(orderId: number): Promise<OrderDtoWithSpecific> {
    const { data: response, error } = await client.GET("/api/adm/orders/{orderId}/detail", {
      params: { path: { orderId } },
    });
    if (error) {
      throw new Error("주문 상세 조회에 실패했습니다.");
    }
    if (!response?.data) {
      throw new Error("주문 상세 정보가 없습니다.");
    }
    // orderItems가 undefined면 []로, productName 등 필드 포함
    return {
      ...response.data,
      orderItems: (response.data.orderItems ?? []).map(item => ({
        ...item,
        productName: item.productName ?? "",
      })),
    };
  }

  // 관리자용 주문 상태 변경
  static async updateOrderStatus(orderId: number, newStatus: OrderStatus): Promise<void> {
    const { error } = await client.PUT("/api/adm/orders/{orderId}/status", {
      params: { path: { orderId } },
      body: { status: newStatus },
    });

    if (error) {
      throw new Error("주문 상태 변경에 실패했습니다.");
    }
  }
} 