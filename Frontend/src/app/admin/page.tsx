"use client";

import Link from "next/link";
import MenuManagement from "../../components/features/admin/MenuManagement";
import { AuthGuard } from "@/src/components/common/AuthGuard";
import { useUser } from "@/src/store/auth";

const AdminPage: React.FC = () => {
  const { user } = useUser();

  return (
    <AuthGuard requireAuth={true}>
      {user?.isAdmin ? (
        <div className="container mx-auto p-8">
          <h1 className="text-3xl font-bold mb-8 text-center">관리자 페이지</h1>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Link href="/admin/menu" className="block">
              <div className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 text-center">
                <h2 className="text-xl font-semibold text-gray-800 mb-2">
                  메뉴 관리
                </h2>
                <p className="text-gray-600">
                  상품 메뉴를 추가, 수정, 삭제합니다.
                </p>
              </div>
            </Link>
            <Link href="/admin/orders" className="block">
              <div className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 text-center">
                <h2 className="text-xl font-semibold text-gray-800 mb-2">
                  주문 관리
                </h2>
                <p className="text-gray-600">
                  들어온 주문 내역을 확인하고 처리합니다.
                </p>
              </div>
            </Link>
          </div>
        </div>
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

export default AdminPage;
