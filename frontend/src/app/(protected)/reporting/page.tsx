"use client";

import { useEffect, useState } from "react";
import { RoleGuard } from "@/components/RoleGuard";
import { useToast } from "@/hooks/useToast";
import { ApiError, getReportingSummary } from "@/lib/api";
import type { ReportingSummary } from "@/lib/types";
import { TICKET_STATUS_LABELS, TICKET_TYPE_LABELS } from "@/lib/types";
import { TICKET_STATUS_COLORS, TICKET_STATUS_ORDER_FOR_CHART, TICKET_TYPE_COLORS } from "@/lib/chartColors";
import { BarChart } from "@/components/BarChart";
import { KpiChip } from "@/components/KpiChip";
import type { TicketType } from "@/lib/types";

export default function ReportingPage() {
  return (
    <RoleGuard allow={["ADMIN", "APPROVER", "ASSIGNER"]}>
      <ReportingContent />
    </RoleGuard>
  );
}

function ReportingContent() {
  const { showToast } = useToast();
  const [summary, setSummary] = useState<ReportingSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getReportingSummary()
      .then(setSummary)
      .catch((err) => showToast(err instanceof ApiError ? err.message : "Failed to load reporting summary."))
      .finally(() => setIsLoading(false));
  }, [showToast]);

  if (isLoading) {
    return <p className="text-sm text-neutral-500 dark:text-neutral-400">Loading...</p>;
  }

  if (!summary) {
    return <p className="text-sm text-neutral-500 dark:text-neutral-400">No reporting data available.</p>;
  }

  const statusRows = TICKET_STATUS_ORDER_FOR_CHART.map((status) => ({
    label: TICKET_STATUS_LABELS[status],
    value: summary.statusCounts[status] ?? 0,
    colorLight: TICKET_STATUS_COLORS[status].light,
    colorDark: TICKET_STATUS_COLORS[status].dark,
  }));

  const typeRows = (Object.keys(TICKET_TYPE_LABELS) as TicketType[]).map((type) => ({
    label: TICKET_TYPE_LABELS[type],
    value: summary.typeCounts[type] ?? 0,
    colorLight: TICKET_TYPE_COLORS[type].light,
    colorDark: TICKET_TYPE_COLORS[type].dark,
  }));

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold">Reporting</h1>

      <div className="flex gap-3">
        <KpiChip label="Total tickets" value={summary.totalTickets} highlight />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="rounded-lg border border-neutral-200 bg-white p-4 dark:border-neutral-800 dark:bg-neutral-900">
          <h2 className="mb-4 text-sm font-semibold">Status Distribution</h2>
          <BarChart rows={statusRows} />
        </div>

        <div className="rounded-lg border border-neutral-200 bg-white p-4 dark:border-neutral-800 dark:bg-neutral-900">
          <h2 className="mb-4 text-sm font-semibold">Ticket Type Breakdown</h2>
          <BarChart rows={typeRows} />
        </div>
      </div>
    </div>
  );
}
