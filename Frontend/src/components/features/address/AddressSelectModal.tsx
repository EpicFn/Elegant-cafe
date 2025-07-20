// src/components/features/home/AddressSelectModal.tsx
import React, { useState, useEffect } from "react";
import { Modal } from "@/src/components/common/Modal";
import { ModalContent } from "@/src/components/common/ModalContent";
import Button from "@/src/components/common/Button";
import ConfirmModal from "@/src/components/features/order/ConfirmModal";
import { useAddressContext } from "@/src/store/address";

interface AddressSelectModalProps {
  open: boolean;
  onClose: () => void;
  currentAddress: string;
  onEditSave: (editedAddress: string) => Promise<void> | void;
  onSelect: (address: string) => Promise<void> | void;
  title?: string;
  confirmMessage?: string;
}

const AddressSelectModal: React.FC<AddressSelectModalProps> = (props) => {
  const { addresses, add } = useAddressContext();
  const [showNewInput, setShowNewInput] = useState(false);
  const [newAddress, setNewAddress] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [editAddress, setEditAddress] = useState(props.currentAddress);
  const [confirm, setConfirm] = useState({
    open: false,
    message: "",
    onConfirm: () => {},
    onCancel: () => {},
  });

  useEffect(() => {
    setEditAddress(props.currentAddress);
  }, [props.currentAddress]);

  if (!props.open) return null;

  // 저장(수정) 버튼 클릭 시 확인 모달
  const handleSaveEdit = () => {
    setConfirm({
      open: true,
      message:
        props.confirmMessage ||
        "배송지가 변경되었습니다. 주소를 등록하시겠습니까?",
      onConfirm: async () => {
        if (!addresses.some((a) => a.content === editAddress)) {
          await add(editAddress);
        }
        await props.onEditSave(editAddress);
        setIsEditing(false);
        setConfirm((prev) => ({ ...prev, open: false }));
      },
      onCancel: async () => {
        await props.onEditSave(editAddress);
        setIsEditing(false);
        setConfirm((prev) => ({ ...prev, open: false }));
      },
    });
  };

  // 새 주소 등록
  const handleRegisterNew = async () => {
    if (!addresses.some((a) => a.content === newAddress)) {
      await add(newAddress);
    }
    setShowNewInput(false);
    setNewAddress("");
  };

  // 주소 선택(배송지 변경) 확인
  const handleSelect = (address: string) => {
    setConfirm({
      open: true,
      message: "주소를 변경하시겠습니까?",
      onConfirm: async () => {
        await props.onSelect(address);
        setConfirm((prev) => ({ ...prev, open: false }));
        props.onClose();
      },
      onCancel: () => setConfirm((prev) => ({ ...prev, open: false })),
    });
  };

  return (
    <Modal onClose={props.onClose} size="base">
      <ModalContent size="base" className="p-6">
        <h2 className="text-xl font-bold mb-4">
          {props.title || "배송지 변경"}
        </h2>
        {/* 현재 배송지 표시 및 수정 */}
        <div className="mb-4">
          <label className="block text-sm font-medium mb-1">현재 배송지</label>
          <div className="flex gap-2 items-center">
            <input
              className={`flex-1 p-3 rounded transition-all text-lg ${
                isEditing
                  ? "border border-gray-300 bg-white focus:outline-amber-500"
                  : props.currentAddress
                  ? "border-none bg-transparent text-2xl font-semibold cursor-default focus:outline-none"
                  : "border-none bg-transparent text-lg text-gray-400 cursor-default"
              }`}
              value={
                isEditing
                  ? editAddress
                  : props.currentAddress
                  ? props.currentAddress
                  : "배송지가 지정되어 있지 않습니다."
              }
              onChange={(e) => setEditAddress(e.target.value)}
              disabled={!isEditing}
              readOnly={!isEditing}
              tabIndex={isEditing ? 0 : -1}
              placeholder={isEditing ? "주소를 입력하세요" : undefined}
            />
            {isEditing ? (
              <>
                <Button
                  text="저장"
                  onClick={() => {
                    if (editAddress.trim()) {
                      handleSaveEdit();
                    }
                  }}
                />
                <Button
                  text="취소"
                  onClick={() => {
                    setEditAddress(props.currentAddress);
                    setIsEditing(false);
                  }}
                />
              </>
            ) : (
              <Button text="수정" onClick={() => setIsEditing(true)} />
            )}
          </div>
        </div>
        {/* 주소 목록 */}
        {!addresses || addresses.length === 0 ? (
          <div className="text-gray-400 text-center py-4">
            등록된 주소가 없습니다.
          </div>
        ) : (
          <ul className="space-y-2 max-h-60 overflow-y-auto mb-4">
            {addresses.map((addr) => (
              <li
                key={addr.id}
                className={`border p-4 rounded cursor-pointer hover:bg-gray-50 flex items-center justify-between transition-colors duration-150 ${
                  props.currentAddress === addr.content
                    ? "border-amber-600 bg-amber-50"
                    : "border-gray-300"
                }`}
                onClick={() => handleSelect(addr.content)}
              >
                <span className="text-lg">{addr.content}</span>
                <div className="flex items-center gap-2">
                  {addr.isDefault && (
                    <span className="ml-2 px-2 py-1 text-xs bg-amber-100 text-amber-700 rounded">
                      기본
                    </span>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
        {/* 새 주소 입력 */}
        {showNewInput ? (
          <div className="flex gap-2 mb-4">
            <input
              className="flex-1 p-3 border rounded"
              value={newAddress ?? ""}
              onChange={(e) => setNewAddress(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && newAddress.trim()) {
                  handleRegisterNew();
                }
              }}
              placeholder="새 주소를 입력하세요"
            />
            <Button
              text="등록"
              onClick={() => newAddress.trim() && handleRegisterNew()}
              disabled={!newAddress.trim()}
            />
            <Button text="취소" onClick={() => setShowNewInput(false)} />
          </div>
        ) : (
          <Button
            text="새 주소 입력"
            onClick={() => setShowNewInput(true)}
            className="w-full mb-4"
          />
        )}
        {/* 닫기 버튼 */}
        <div className="flex justify-end mt-2">
          <Button text="닫기" onClick={props.onClose} />
        </div>
      </ModalContent>
      {/* 확인 모달 */}
      {confirm.open && (
        <ConfirmModal
          open={confirm.open}
          onClose={confirm.onCancel}
          onConfirm={confirm.onConfirm}
          message={confirm.message}
        />
      )}
    </Modal>
  );
};

export default AddressSelectModal;
