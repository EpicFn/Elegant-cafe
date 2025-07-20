"use client";

import { useState, useEffect } from "react";
import Button from "@/src/components/common/Button";
import { Plus, Pencil, Trash2, Star } from "lucide-react";
import ConfirmModal from "@/src/components/features/order/ConfirmModal";
import CompleteModal from "@/src/components/features/order/CompleteModal";
import { useAddressContext } from "@/src/store/address";

export default function AddressPanel() {
  // ───────────────────────────────
  // 상태 관리
  const { addresses, add, edit, remove, setDefault } = useAddressContext();

  const [editingId, setEditingId] = useState<number | null>(null);
  const [inputValue, setInputValue] = useState("");
  const [isAdding, setIsAdding] = useState(false);
  const [confirm, setConfirm] = useState({
    open: false,
    onConfirm: () => {},
    message: "",
  });
  // CompleteModal 상태 관리
  const [completeOpen, setCompleteOpen] = useState(false);
  const [completeMessage, setCompleteMessage] = useState("");

  // ───────────────────────────────
  // 제어 함수
  const resetForm = () => {
    setInputValue("");
    setEditingId(null);
    setIsAdding(false);
  };

  const openConfirm = (message: string, onConfirm: () => void) => {
    setConfirm({ open: true, onConfirm, message });
  };

  // ───────────────────────────────
  // 핸들러 함수
  const handleAdd = async () => {
    if (!inputValue.trim()) return;
    try {
      await add(inputValue.trim());
      resetForm();
      setCompleteMessage("주소가 등록되었습니다.");
      setCompleteOpen(true);
    } catch (error) {
      console.error("주소 등록록 실패:", error);
      setCompleteMessage("주소 추가에 실패했습니다.");
      setCompleteOpen(true);
    }
  };

  const handleEdit = async (id: number) => {
    if (!inputValue.trim()) return;
    try {
      await edit(id, inputValue.trim());
      resetForm();
      setCompleteMessage("주소가 수정되었습니다.");
      setCompleteOpen(true);
    } catch (error) {
      console.error("주소 수정 실패:", error);
      setCompleteMessage("주소 수정에 실패했습니다.");
      setCompleteOpen(true);
    }
  };

  const handleDelete = (id: number) => {
    openConfirm("이 주소를 삭제하시겠습니까?", async () => {
      try {
        await remove(id);
        setCompleteMessage("주소가 삭제되었습니다.");
        setCompleteOpen(true);
      } catch (error) {
        console.error("주소 삭제 실패:", error);
        setCompleteMessage("주소 삭제에 실패했습니다.");
        setCompleteOpen(true);
      }
    });
  };

  const handleSetDefault = (id: number) => {
    openConfirm("기본 배송지를 변경하시겠습니까?", async () => {
      try {
        await setDefault(id);
        setCompleteMessage("기본 배송지가 변경되었습니다.");
        setCompleteOpen(true);
      } catch (error) {
        console.error("기본 주소 설정 실패:", error);
        setCompleteMessage("기본 주소 설정에 실패했습니다.");
        setCompleteOpen(true);
      }
    });
  };

  // ───────────────────────────────
  // 렌더링
  // 기본 배송지가 항상 맨 위로 오도록 정렬
  const sortedAddresses = [...addresses].sort(
    (a, b) => (b.isDefault ? 1 : 0) - (a.isDefault ? 1 : 0)
  );

  return (
    <section className="bg-white shadow p-6 rounded">
      {/* 헤더 */}
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold">주소 관리</h2>
        <Button
          icon={Plus}
          text="주소 추가"
          onClick={() => {
            resetForm();
            setIsAdding(true);
          }}
          className="border-amber-600 text-amber-600 hover:bg-amber-50"
        />
      </div>

      {/* 주소 목록 */}
      <ul className="space-y-4">
        {sortedAddresses.map((addr) =>
          editingId === addr.id ? (
            // 수정 폼
            <li
              key={addr.id}
              className="border border-dashed border-gray-300 rounded p-4"
            >
              <div className="flex items-center gap-2 w-full">
                <input
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                  placeholder={addr.content}
                  className="flex-1 p-2 border rounded"
                />
                <Button text="저장" onClick={() => handleEdit(addr.id)} />
                <Button text="취소" onClick={resetForm} />
              </div>
            </li>
          ) : (
            // 주소 아이템
            <li key={addr.id} className="border border-gray-200 rounded p-4">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-lg text-gray-900 font-medium">
                    {addr.content}
                    {addr.isDefault && (
                      <span className="text-sm text-amber-600 ml-2">
                        (기본 배송지)
                      </span>
                    )}
                  </p>
                </div>
                <div className="flex gap-2">
                  {!addr.isDefault && (
                    <Button
                      icon={Star}
                      text="기본으로 설정"
                      onClick={() => handleSetDefault(addr.id)}
                      className="text-sm"
                    />
                  )}
                  <Button
                    icon={Pencil}
                    text="수정"
                    onClick={() => {
                      resetForm();
                      setEditingId(addr.id);
                      setInputValue(addr.content);
                    }}
                    className="text-sm"
                  />
                  <Button
                    icon={Trash2}
                    text="삭제"
                    onClick={() => handleDelete(addr.id)}
                    className="text-sm text-red-600 border-red-300 hover:bg-red-50"
                  />
                </div>
              </div>
            </li>
          )
        )}

        {/* 주소 추가 입력창 */}
        {isAdding && (
          <li className="border border-dashed border-gray-300 rounded p-4">
            <div className="flex items-center gap-2 w-full">
              <input
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                className="flex-1 p-2 border rounded"
                placeholder="주소를 입력해 주세요"
              />
              <Button text="저장" onClick={handleAdd} />
              <Button text="취소" onClick={resetForm} />
            </div>
          </li>
        )}
      </ul>

      {/* 확인 모달 */}
      {confirm.open && (
        <ConfirmModal
          open={confirm.open}
          onClose={() => setConfirm({ ...confirm, open: false })}
          onConfirm={() => {
            confirm.onConfirm();
            setConfirm({ ...confirm, open: false });
          }}
          message={confirm.message}
        />
      )}
      {/* 완료 모달 */}
      <CompleteModal
        open={completeOpen}
        onClose={() => setCompleteOpen(false)}
        message={completeMessage}
      />
    </section>
  );
}
