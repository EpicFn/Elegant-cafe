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
import { OrderService } from "@/src/services/orderService";
import { components } from "@/src/lib/backend/api/schema";

export type OrderStatus = components["schemas"]["OrderDto"]["state"];

// 서버 스키마 기반 + 프론트 전용 상품명(name)만 추가
export interface OrderItem {
  id: number; // 주문 아이템 ID
  orderId: number;
  productId: number;
  count: number;
  price: number;
  /**
   * 프론트 전용: 상품명 (실제 서버 컬럼명은 name, itemName 등으로 변경될 수 있음)
   * 실제 API 연동 시 서버에서 내려주는 필드명에 맞게 교체 필요
   */
  name?: string;
}

export interface Order {
  id: number;
  customerEmail: string;
  createdDate: string;
  state: OrderStatus;
  customerAddress: string;
  orderItems: OrderItem[];
}

export interface CreateOrderData {
  customerEmail: string;
  customerAddress: string;
  orderItems: { productId: number; count: number }[];
}

interface OrderContextType {
  orders: Order[];
  loading: boolean;
  error: string | null;
  fetchOrders: () => Promise<void>;
  fetchOrderDetail: (orderId: number) => Promise<Order>;
  createOrder: (orderData: CreateOrderData) => Promise<Order>;
  cancelOrder: (orderId: number) => Promise<void>;
  updateOrderAddress: (orderId: number, address: string) => Promise<void>;
  getOrderById: (orderId: number) => Order | undefined;
  getOrdersByStatus: (state: OrderStatus) => Order[];
}

const OrderContext = createContext<OrderContextType | undefined>(undefined);

export function OrderProvider({ children }: { children: ReactNode }) {
  const { user } = useUser();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 회원 주문 목록 조회
  const fetchOrders = useCallback(async () => {
    if (!user) {
      setOrders([]);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const memberOrders = await OrderService.getMemberOrders();
      // 서버 응답을 프론트엔드 Order 타입으로 변환
      const convertedOrders: Order[] = memberOrders.map((order) => ({
        id: order.orderId,
        customerEmail: user?.email || "", // 현재 로그인한 사용자 이메일 사용
        createdDate: order.orderDate,
        state: order.status as OrderStatus,
        customerAddress: order.customerAddress,
        orderItems: order.orderItems.map((item, idx) => ({
          id: idx, // 임시 ID
          orderId: order.orderId,
          productId: item.productId,
          count: item.count,
          price: item.price,
          name: item.productName,
        })),
      }));
      setOrders(convertedOrders);
    } catch (err) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "주문 목록을 불러오는데 실패했습니다.";
      setError(errorMessage);
      console.error("주문 목록 조회 실패:", err);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, [user]);

  // 사용자 로그아웃 시 주문 목록 초기화
  useEffect(() => {
    if (!user) {
      setOrders([]);
    }
  }, [user]);

  // 회원 주문 상세 조회
  const fetchOrderDetail = useCallback(
    async (orderId: number): Promise<Order> => {
      if (!user) {
        throw new Error("로그인이 필요합니다.");
      }

      setLoading(true);
      setError(null);
      try {
        const orderDetail = await OrderService.getMemberOrderDetail(orderId);

        // 서버 응답을 프론트엔드 Order 타입으로 변환
        const convertedOrder: Order = {
          id: orderDetail.orderId,
          customerEmail: user?.email || "",
          createdDate: orderDetail.orderDate,
          state: orderDetail.status as OrderStatus,
          customerAddress: orderDetail.customerAddress,
          orderItems: orderDetail.orderItems.map((item, idx) => ({
            id: idx,
            orderId: orderDetail.orderId,
            productId: item.productId,
            count: item.count,
            price: item.price,
            name: item.productName,
          })),
        };

        return convertedOrder;
      } catch (err) {
        const errorMessage =
          err instanceof Error
            ? err.message
            : "주문 상세 정보를 불러오는데 실패했습니다.";
        setError(errorMessage);
        console.error("주문 상세 조회 실패:", err);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [user]
  );

  // 주문 생성
  const createOrder = useCallback(
    async (orderData: CreateOrderData): Promise<Order> => {
      setLoading(true);
      setError(null);
      try {
        // 서버에 주문 생성 요청
        const createdOrder = await OrderService.createOrder({
          customerAddress: orderData.customerAddress,
          orderItems: orderData.orderItems,
        });

        // 주문 목록 새로고침 (서버에서 최신 데이터 조회)
        await fetchOrders();

        // 서버 응답에서 생성된 주문 정보 반환
        // orderItems가 없을 경우 요청 데이터를 사용
        return {
          id: createdOrder.id,
          customerEmail: orderData.customerEmail,
          createdDate: createdOrder.createdDate,
          state: createdOrder.state,
          customerAddress: createdOrder.customerAddress,
          orderItems:
            createdOrder.orderItems?.map((item, idx) => ({
              id: idx,
              orderId: createdOrder.id,
              productId: item.productId,
              count: item.count,
              price: item.price,
              name: `상품${item.productId}`, // 서버에서 name이 없으므로 임시 처리
            })) ||
            orderData.orderItems.map((item, idx) => ({
              id: idx,
              orderId: createdOrder.id,
              productId: item.productId,
              count: item.count,
              price: 0, // 서버에서 가격 정보가 없으므로 0으로 설정
              name: `상품${item.productId}`,
            })),
        };
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "주문 생성에 실패했습니다.";
        setError(errorMessage);
        console.error("주문 생성 실패:", err);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [fetchOrders]
  );

  // 주문 취소
  const cancelOrder = useCallback(
    async (orderId: number) => {
      setLoading(true);
      setError(null);
      try {
        // 서버에 주문 취소 요청
        await OrderService.cancelOrder(orderId);

        // 주문 목록 새로고침 (서버에서 최신 데이터 조회)
        await fetchOrders();
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "주문 취소에 실패했습니다.";
        setError(errorMessage);
        console.error("주문 취소 실패:", err);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [fetchOrders]
  );

  // 주문 배송지 변경
  const updateOrderAddress = useCallback(
    async (orderId: number, address: string) => {
      setLoading(true);
      setError(null);
      try {
        // 서버에 배송지 변경 요청
        await OrderService.updateOrderAddress(orderId, address);

        // 주문 목록 새로고침 (서버에서 최신 데이터 조회)
        await fetchOrders();
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "배송지 변경에 실패했습니다.";
        setError(errorMessage);
        console.error("배송지 변경 실패:", err);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [fetchOrders]
  );

  const getOrderById = useCallback(
    (orderId: number): Order | undefined => {
      return orders.find((order) => order.id === orderId);
    },
    [orders]
  );

  const getOrdersByStatus = useCallback(
    (state: string): Order[] => {
      return orders.filter((order) => order.state === state);
    },
    [orders]
  );

  return (
    <OrderContext.Provider
      value={{
        orders,
        loading,
        error,
        fetchOrders,
        fetchOrderDetail,
        createOrder,
        cancelOrder,
        updateOrderAddress,
        getOrderById,
        getOrdersByStatus,
      }}
    >
      {children}
    </OrderContext.Provider>
  );
}

export function useOrders() {
  const context = useContext(OrderContext);
  if (context === undefined) {
    throw new Error("useOrders must be used within an OrderProvider");
  }
  return context;
}
