"use client";

import { useEffect } from "react";
import Button from "@/src/components/common/Button";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-4">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">
          문제가 발생했습니다
        </h2>
        <p className="text-gray-600 mb-6">
          예상치 못한 오류가 발생했습니다. 다시 시도해주세요.
        </p>
        <div className="space-x-4">
          <Button text="다시 시도" onClick={reset} />
          <Button
            text="홈으로 이동"
            onClick={() => (window.location.href = "/")}
          />
        </div>
      </div>
    </div>
  );
}
