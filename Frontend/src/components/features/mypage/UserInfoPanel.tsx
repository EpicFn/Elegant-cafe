import Button from "@/src/components/common/Button";
import { Pencil } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useUser } from "@/src/store/auth";
import { useAddressContext } from "@/src/store/address";

// 마이페이지 - 회원 정보 패널
// UserContext에서 사용자 정보(user)를 가져와 표시
// 정보 수정 버튼 클릭 시 /member/mypage/edit로 이동
export default function UserInfoPanel() {
  const router = useRouter();
  const { user, fetchUserInfo } = useUser();
  const { addresses } = useAddressContext();

  // user 정보가 없으면 fetchUserInfo로 불러옴
  useEffect(() => {
    if (!user) {
      fetchUserInfo();
    }
  }, [user, fetchUserInfo]);

  // 로딩 상태 처리
  if (!user) {
    return <div>로딩 중...</div>;
  }

  const defaultAddress = addresses.find((a) => a.isDefault);

  return (
    <section className="bg-white shadow p-6 rounded">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold">회원 정보</h2>
        {/* 정보 수정 버튼: 클릭 시 회원 정보 수정 페이지로 이동 */}
        <Button
          icon={Pencil}
          text="정보 수정하기"
          onClick={() => router.push("/member/mypage/edit")}
          className="border-amber-600 text-amber-600 hover:bg-amber-50"
        />
      </div>
      {/* 회원 정보 표시 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm text-gray-500">이름</label>
          <p className="mt-2 text-lg text-gray-900">{user.name}</p>
        </div>
        <div>
          <label className="block text-sm text-gray-500">이메일</label>
          <p className="mt-2 text-lg text-gray-900">{user.email}</p>
        </div>
        <div className="md:col-span-2">
          <label className="block text-sm text-gray-500">기본 배송지</label>
          <p className="mt-2 text-lg text-gray-900">
            {defaultAddress
              ? defaultAddress.content
              : "등록된 주소가 없습니다."}
          </p>
        </div>
      </div>
    </section>
  );
}
