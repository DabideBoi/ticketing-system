import type { Ticket, TicketStatus } from "@/lib/types";

export interface ProgressStep {
  status: TicketStatus;
  state: "done" | "active" | "pending";
  label?: string;
}

export function buildProgressSteps(ticket: Ticket): ProgressStep[] {
  const wasRejected =
    ticket.type === "DB_FIX" &&
    ticket.status === "CLOSE" &&
    !ticket.auditTrail.some((entry) => entry.toStatus === "FOR_ASSIGNMENT");

  if (wasRejected) {
    return [
      { status: "FOR_APPROVAL", state: "done" },
      { status: "CLOSE", state: "done", label: "Rejected" },
    ];
  }

  const base: TicketStatus[] =
    ticket.type === "DB_FIX"
      ? ["FOR_APPROVAL", "FOR_ASSIGNMENT", "ASSIGNED", "ONGOING", "FOR_CLOSE", "CLOSE"]
      : ["FOR_ASSIGNMENT", "ASSIGNED", "ONGOING", "FOR_CLOSE", "CLOSE"];

  const currentIndex = base.indexOf(ticket.status);

  return base.map((status, i) => ({
    status,
    state: i < currentIndex ? "done" : i === currentIndex ? "active" : "pending",
  }));
}
