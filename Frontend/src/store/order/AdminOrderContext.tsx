import React, {
  createContext,
  useContext,
  useState,
  useCallback,
  ReactNode,
} from "react";
import type { components } from "@/src/lib/backend/api/schema.d.ts";
type AdminOrder = components["schemas"]["OrderDtoWithName"];
type OrderStatus = AdminOrder["state"];
import { AdminService } from "@/src/services/adminService";

interface AdminOrderContextType {
  orders: AdminOrder[];
  loading: boolean;
  error: string | null;
  fetchAdminOrders: () => Promise<void>;
  updateOrderStatus: (orderId: number, newStatus: OrderStatus) => Promise<void>;
}

const AdminOrderContext = createContext<AdminOrderContextType | undefined>(
  undefined
);

export const AdminOrderProvider = ({ children }: { children: ReactNode }) => {
  const [orders, setOrders] = useState<AdminOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 관리자 주문 목록 조회
  const fetchAdminOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const adminOrders = await AdminService.getOrders();
      setOrders(
        adminOrders.map((order) => ({
          ...order,
          orderItems: (order.orderItems ?? []).map((item) => ({
            ...item,
            productName: item.productName ?? "",
          })),
        }))
      );
    } catch (err) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "관리자 주문 목록을 불러오는데 실패했습니다.";
      setError(errorMessage);
      setOrders([]);
      console.error("관리자 주문 목록 조회 실패:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  // 관리자 주문 상태 변경
  const updateOrderStatus = useCallback(
    async (orderId: number, newStatus: OrderStatus) => {
      setLoading(true);
      setError(null);
      try {
        await AdminService.updateOrderStatus(orderId, newStatus);
        await fetchAdminOrders(); // 상태 변경 후 목록 새로고침
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : "주문 상태 변경에 실패했습니다.";
        setError(errorMessage);
        console.error("주문 상태 변경 실패:", err);
      } finally {
        setLoading(false);
      }
    },
    [fetchAdminOrders]
  );

  return (
    <AdminOrderContext.Provider
      value={{ orders, loading, error, fetchAdminOrders, updateOrderStatus }}
    >
      {children}
    </AdminOrderContext.Provider>
  );
};

export const useAdminOrders = () => {
  const context = useContext(AdminOrderContext);
  if (context === undefined) {
    throw new Error("useAdminOrders must be used within an AdminOrderProvider");
  }
  return context;
};
