interface Props {
  activeTab: string;
  setActiveTab: (tab: "info" | "address" | "orders" | "settings") => void;
}

export default function MypageTabs({ activeTab, setActiveTab }: Props) {
  const tabs = [
    { id: "info", label: "회원 정보" },
    { id: "address", label: "주소 관리" },
    { id: "orders", label: "주문 내역" },
    { id: "settings", label: "설정" },
  ];

  return (
    <nav className="border-b border-gray-200 flex space-x-4">
      {tabs.map((tab) => {
        const isActive = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={`px-4 py-2 text-base font-medium ${
              isActive
                ? "border-b-2 border-amber-600 text-amber-600"
                : "text-gray-600 hover:text-amber-600 hover:border-b-2 hover:border-amber-300"
            }`}
          >
            {tab.label}
          </button>
        );
      })}
    </nav>
  );
}
