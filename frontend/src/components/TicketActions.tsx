"use client";

import { useEffect, useState } from "react";
import { approveTicket, assignTicket, getUsersByRole, updateTicketStatus, ApiError } from "@/lib/api";
import { useToast } from "@/hooks/useToast";
import type { Ticket, User } from "@/lib/types";

export function TicketActions({
  ticket,
  currentUser,
  onChanged,
}: {
  ticket: Ticket;
  currentUser: User;
  onChanged: () => void;
}) {
  const { showToast } = useToast();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [remarks, setRemarks] = useState("");
  const [assignees, setAssignees] = useState<User[]>([]);
  const [selectedAssignee, setSelectedAssignee] = useState("");

  const role = currentUser.role;

  useEffect(() => {
    if (ticket.status === "FOR_ASSIGNMENT" && ["ASSIGNER", "APPROVER", "ADMIN"].includes(role)) {
      getUsersByRole("ASSIGNEE")
        .then(setAssignees)
        .catch(() => showToast("Failed to load assignable users."));
    }
  }, [ticket.status, role, showToast]);

  async function run(action: () => Promise<Ticket>) {
    setIsSubmitting(true);
    try {
      await action();
      showToast("Ticket updated.", "success");
      onChanged();
    } catch (err) {
      showToast(err instanceof ApiError ? err.message : "Action failed.");
    } finally {
      setIsSubmitting(false);
    }
  }

  const canApprove = ticket.status === "FOR_APPROVAL" && ["APPROVER", "ADMIN"].includes(role);
  const canAssign = ticket.status === "FOR_ASSIGNMENT" && ["ASSIGNER", "APPROVER", "ADMIN"].includes(role);
  const canStartWork = ticket.status === "ASSIGNED" && ["ASSIGNEE", "ADMIN"].includes(role);
  const canResolve = ticket.status === "ONGOING" && ["ASSIGNEE", "ADMIN"].includes(role);
  const canClose = ticket.status === "FOR_CLOSE" && ["REQUESTOR", "ASSIGNER", "ADMIN"].includes(role);

  if (!canApprove && !canAssign && !canStartWork && !canResolve && !canClose) {
    return null;
  }

  return (
    <div className="rounded-lg border border-neutral-200 bg-white p-4 dark:border-neutral-800 dark:bg-neutral-900">
      <h3 className="mb-3 text-sm font-semibold">Actions</h3>

      {canApprove && (
        <div className="flex flex-col gap-3">
          <textarea
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="Remarks (optional)"
            rows={2}
            className="w-full rounded-lg border border-neutral-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none dark:border-neutral-700 dark:bg-neutral-800"
          />
          <div className="flex gap-2">
            <button
              disabled={isSubmitting}
              onClick={() => run(() => approveTicket(ticket.id, { approved: true, remarks }))}
              className="rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
            >
              Approve
            </button>
            <button
              disabled={isSubmitting}
              onClick={() => run(() => approveTicket(ticket.id, { approved: false, remarks }))}
              className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
            >
              Reject
            </button>
          </div>
        </div>
      )}

      {canAssign && (
        <div className="flex gap-2">
          <select
            value={selectedAssignee}
            onChange={(e) => setSelectedAssignee(e.target.value)}
            className="flex-1 rounded-lg border border-neutral-300 px-3 py-2 text-sm dark:border-neutral-700 dark:bg-neutral-800"
          >
            <option value="">Select an assignee...</option>
            {assignees.map((a) => (
              <option key={a.id} value={a.id}>
                {a.fullName}
              </option>
            ))}
          </select>
          <button
            disabled={isSubmitting || !selectedAssignee}
            onClick={() => run(() => assignTicket(ticket.id, selectedAssignee))}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            Assign
          </button>
        </div>
      )}

      {canStartWork && (
        <button
          disabled={isSubmitting}
          onClick={() => run(() => updateTicketStatus(ticket.id, "ONGOING"))}
          className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          Start Work
        </button>
      )}

      {canResolve && (
        <button
          disabled={isSubmitting}
          onClick={() => run(() => updateTicketStatus(ticket.id, "FOR_CLOSE"))}
          className="rounded-lg bg-orange-600 px-4 py-2 text-sm font-medium text-white hover:bg-orange-700 disabled:opacity-50"
        >
          Resolve
        </button>
      )}

      {canClose && (
        <button
          disabled={isSubmitting}
          onClick={() => run(() => updateTicketStatus(ticket.id, "CLOSE"))}
          className="rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
        >
          Close Ticket
        </button>
      )}
    </div>
  );
}
