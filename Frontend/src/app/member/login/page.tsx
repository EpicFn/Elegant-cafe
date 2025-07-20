"use client";
import { useRouter } from "next/navigation";
import { LoginModal } from "@/src/components/features/auth/LoginModal";
import { AuthGuard } from "@/src/components/common/AuthGuard";

export default function LoginPage() {
  const router = useRouter();

  return (
    <AuthGuard requireAuth={false}>
      <LoginModal
        onClose={() => router.push("/")}
        onLoginSuccess={() => router.push("/")}
      />
    </AuthGuard>
  );
}
