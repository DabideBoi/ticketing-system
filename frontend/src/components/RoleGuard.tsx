"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import type { Role } from "@/lib/types";

export function RoleGuard({ allow, children }: { allow: Role[]; children: React.ReactNode }) {
  const { user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (user && !allow.includes(user.role)) {
      router.replace("/dashboard");
    }
  }, [user, allow, router]);

  if (!user || !allow.includes(user.role)) {
    return null;
  }

  return <>{children}</>;
}
