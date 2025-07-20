"use client";
import { useRouter } from "next/navigation";
import { SignupModal } from "@/src/components/features/auth/SignupModal";
import { AuthGuard } from "@/src/components/common/AuthGuard";

export default function SignupPage() {
  const router = useRouter();

  return (
    <AuthGuard requireAuth={false}>
      <SignupModal
        onClose={() => router.push("/")}
        onSignupSuccess={() => router.push("/")}
      />
    </AuthGuard>
  );
}
