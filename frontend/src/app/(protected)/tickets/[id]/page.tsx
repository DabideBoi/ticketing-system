"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { useToast } from "@/hooks/useToast";
import { ApiError, getTicket } from "@/lib/api";
import type { Ticket } from "@/lib/types";
import { TICKET_TYPE_LABELS } from "@/lib/types";
import { buildProgressSteps } from "@/lib/progress";
import { StatusBadge } from "@/components/StatusBadge";
import { ProgressRail } from "@/components/ProgressRail";
import { AuditTrail } from "@/components/AuditTrail";
import { TicketActions } from "@/components/TicketActions";

export default function TicketDetailPage() {
  const params = useParams<{ id: string }>();
  const { user } = useAuth();
  const { showToast } = useToast();
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const load = useCallback(async () => {
    try {
      const data = await getTicket(params.id);
      setTicket(data);
    } catch (err) {
      showToast(err instanceof ApiError ? err.message : "Failed to load ticket.");
    } finally {
      setIsLoading(false);
    }
  }, [params.id, showToast]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- initial data fetch on mount, and after actions via onChanged
    load();
  }, [load]);

  if (isLoading) {
    return <p className="text-sm text-neutral-500 dark:text-neutral-400">Loading...</p>;
  }

  if (!ticket || !user) {
    return <p className="text-sm text-neutral-500 dark:text-neutral-400">Ticket not found.</p>;
  }

  const steps = buildProgressSteps(ticket);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-lg font-semibold">
            {ticket.title} <span className="text-neutral-400">· {TICKET_TYPE_LABELS[ticket.type]}</span>
          </h1>
          <p className="text-sm text-neutral-500 dark:text-neutral-400">{ticket.description}</p>
        </div>
        <StatusBadge status={ticket.status} />
      </div>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-[auto_1fr]">
        <div className="rounded-lg border border-neutral-200 bg-white p-4 dark:border-neutral-800 dark:bg-neutral-900">
          <ProgressRail steps={steps} />
        </div>

        <div className="flex flex-col gap-6">
          <div className="rounded-lg border border-neutral-200 bg-white p-4 dark:border-neutral-800 dark:bg-neutral-900">
            <h2 className="mb-3 text-sm font-semibold">Audit Trail</h2>
            <AuditTrail entries={ticket.auditTrail} />
          </div>

          <TicketActions ticket={ticket} currentUser={user} onChanged={load} />

          <dl className="grid grid-cols-2 gap-3 text-sm md:grid-cols-3">
            <Info label="Requestor" value={ticket.requestor?.fullName} />
            <Info label="Approver" value={ticket.approver?.fullName} />
            <Info label="Assignee" value={ticket.assignee?.fullName} />
          </dl>
        </div>
      </div>
    </div>
  );
}

function Info({ label, value }: { label: string; value?: string | null }) {
  return (
    <div>
      <dt className="text-xs text-neutral-500 dark:text-neutral-400">{label}</dt>
      <dd className="font-medium">{value ?? "—"}</dd>
    </div>
  );
}
