"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { useToast } from "@/hooks/useToast";
import { getDashboardActivity, getDashboardStats, ApiError } from "@/lib/api";
import type { DashboardStats } from "@/lib/types";
import type { ActivityItem } from "@/lib/activity";
import { KpiChip } from "@/components/KpiChip";
import { ActivityFeed } from "@/components/ActivityFeed";

export default function DashboardPage() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [activity, setActivity] = useState<ActivityItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [statsData, activityData] = await Promise.all([getDashboardStats(), getDashboardActivity()]);
        setStats(statsData);
        setActivity(activityData);
      } catch (err) {
        showToast(err instanceof ApiError ? err.message : "Failed to load dashboard data.");
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, [showToast]);

  if (!user) return null;

  const chips = buildChipsForRole(user.role, stats);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold">Activity · {roleLabel(user.role)}</h1>
        <p className="text-sm text-neutral-500 dark:text-neutral-400">Welcome back, {user.fullName}.</p>
      </div>

      {isLoading ? (
        <p className="text-sm text-neutral-500 dark:text-neutral-400">Loading...</p>
      ) : (
        <>
          {chips.length > 0 && (
            <div className="flex gap-3">
              {chips.map((chip) => (
                <KpiChip key={chip.label} label={chip.label} value={chip.value} highlight={chip.highlight} />
              ))}
            </div>
          )}

          <div>
            <h2 className="mb-3 text-sm font-semibold text-neutral-600 dark:text-neutral-300">Recent Activity</h2>
            <ActivityFeed items={activity} />
          </div>
        </>
      )}
    </div>
  );
}

function roleLabel(role: string) {
  return role.charAt(0) + role.slice(1).toLowerCase();
}

function buildChipsForRole(role: string, stats: DashboardStats | null) {
  if (!stats) return [];
  switch (role) {
    case "REQUESTOR":
      return [{ label: "My open tickets", value: stats.myOpenTickets, highlight: true }];
    case "APPROVER":
      return [{ label: "Pending approvals", value: stats.pendingApprovals, highlight: true }];
    case "ASSIGNER":
      return [{ label: "For assignment", value: stats.forAssignment, highlight: true }];
    case "ASSIGNEE":
      return [
        { label: "Assigned to me", value: stats.assignedToMe, highlight: true },
        { label: "Ongoing", value: stats.ongoing },
      ];
    case "ADMIN":
      return [
        { label: "My open tickets", value: stats.myOpenTickets },
        { label: "Pending approvals", value: stats.pendingApprovals, highlight: true },
        { label: "For assignment", value: stats.forAssignment },
        { label: "Ongoing", value: stats.ongoing },
      ];
    default:
      return [];
  }
}
