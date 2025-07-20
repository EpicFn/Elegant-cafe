"use client";

import OrderManagement from "@/src/components/features/admin/OrderManagement";
import { AuthGuard } from "@/src/components/common/AuthGuard";
import { useUser } from "@/src/store/auth";
import { AdminOrderProvider } from "@/src/store/order/AdminOrderContext";

const AdminOrderPage: React.FC = () => {
  const { user } = useUser();

  return (
    <AuthGuard requireAuth={true}>
      {user?.isAdmin ? (
        <AdminOrderProvider>
          <OrderManagement />
        </AdminOrderProvider>
      ) : (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-red-600 mb-4">
              접근 권한이 없습니다
            </h1>
            <p className="text-gray-600">
              관리자만 접근할 수 있는 페이지입니다.
            </p>
          </div>
        </div>
      )}
    </AuthGuard>
  );
};

export default AdminOrderPage;
