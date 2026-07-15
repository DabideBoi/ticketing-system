"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, PlusCircle, BarChart3, LogOut, Ticket } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import type { Role } from "@/lib/types";

const REPORTING_ROLES: Role[] = ["ADMIN", "APPROVER", "ASSIGNER"];
const CREATE_ROLES: Role[] = ["REQUESTOR", "ADMIN"];

export function NavBar() {
  const { user, logout } = useAuth();
  const pathname = usePathname();

  if (!user) return null;

  const links = [
    { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard, show: true },
    { href: "/tickets/new", label: "New Request", icon: PlusCircle, show: CREATE_ROLES.includes(user.role) },
    { href: "/reporting", label: "Reporting", icon: BarChart3, show: REPORTING_ROLES.includes(user.role) },
  ];

  return (
    <header className="border-b border-neutral-200 bg-white dark:border-neutral-800 dark:bg-neutral-900">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <div className="flex items-center gap-6">
          <Link href="/dashboard" className="flex items-center gap-2 font-semibold">
            <Ticket className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            Ticketing
          </Link>
          <nav className="flex items-center gap-1">
            {links
              .filter((link) => link.show)
              .map((link) => {
                const Icon = link.icon;
                const active = pathname === link.href;
                return (
                  <Link
                    key={link.href}
                    href={link.href}
                    className={`flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
                      active
                        ? "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300"
                        : "text-neutral-600 hover:bg-neutral-100 dark:text-neutral-300 dark:hover:bg-neutral-800"
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    {link.label}
                  </Link>
                );
              })}
          </nav>
        </div>
        <div className="flex items-center gap-3">
          <div className="text-right text-sm">
            <div className="font-medium">{user.fullName}</div>
            <div className="text-xs text-neutral-500 dark:text-neutral-400">{user.role}</div>
          </div>
          <button
            onClick={logout}
            className="rounded-lg p-2 text-neutral-500 hover:bg-neutral-100 hover:text-neutral-700 dark:text-neutral-400 dark:hover:bg-neutral-800"
            aria-label="Logout"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </header>
  );
}
