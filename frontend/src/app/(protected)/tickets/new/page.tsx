"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight } from "lucide-react";
import { RoleGuard } from "@/components/RoleGuard";
import { useToast } from "@/hooks/useToast";
import { ApiError, createTicket } from "@/lib/api";
import { TICKET_TYPE_LABELS, type TicketType } from "@/lib/types";

const TICKET_TYPES = Object.keys(TICKET_TYPE_LABELS) as TicketType[];

export default function NewTicketPage() {
  return (
    <RoleGuard allow={["REQUESTOR", "ADMIN"]}>
      <NewTicketForm />
    </RoleGuard>
  );
}

function NewTicketForm() {
  const router = useRouter();
  const { showToast } = useToast();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [type, setType] = useState<TicketType>("SERVICE_REQUEST");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isDbFix = type === "DB_FIX";

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      const ticket = await createTicket({ title, description, type });
      showToast("Ticket created.", "success");
      router.push(`/tickets/${ticket.id}`);
    } catch (err) {
      showToast(err instanceof ApiError ? err.message : "Failed to create ticket.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="mb-6 text-xl font-semibold">New Request</h1>

      <form onSubmit={handleSubmit} className="flex flex-col gap-5">
        <div>
          <label className="mb-1 block text-sm font-medium">Ticket Type</label>
          <div className="flex flex-wrap gap-2">
            {TICKET_TYPES.map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setType(t)}
                className={`rounded-full border px-3 py-1.5 text-sm font-medium transition-colors ${
                  type === t
                    ? "border-blue-500 bg-blue-50 text-blue-700 dark:border-blue-600 dark:bg-blue-950 dark:text-blue-300"
                    : "border-neutral-300 text-neutral-600 hover:bg-neutral-50 dark:border-neutral-700 dark:text-neutral-300 dark:hover:bg-neutral-800"
                }`}
              >
                {TICKET_TYPE_LABELS[t]}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label htmlFor="title" className="mb-1 block text-sm font-medium">
            Title
          </label>
          <input
            id="title"
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full rounded-lg border border-neutral-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none dark:border-neutral-700 dark:bg-neutral-800"
            placeholder="Brief summary of the request"
          />
        </div>

        <div>
          <label htmlFor="description" className="mb-1 block text-sm font-medium">
            Description
          </label>
          <textarea
            id="description"
            required
            rows={5}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full rounded-lg border border-neutral-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none dark:border-neutral-700 dark:bg-neutral-800"
            placeholder="Details of your request"
          />
        </div>

        <div className="rounded-lg bg-neutral-100 p-4 dark:bg-neutral-800/60">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-neutral-500 dark:text-neutral-400">
            What happens next
          </p>
          <div className="flex flex-wrap items-center gap-2 text-sm">
            {isDbFix && (
              <>
                <RouteChip label="Approver" color="amber" />
                <ArrowRight className="h-4 w-4 text-neutral-400" />
              </>
            )}
            <RouteChip label="Assigner" color="blue" />
            <ArrowRight className="h-4 w-4 text-neutral-400" />
            <RouteChip label="Assignee" color="green" />
          </div>
          {isDbFix && (
            <p className="mt-2 text-xs text-neutral-500 dark:text-neutral-400">
              DB Fix requests require approval before an assignee is picked.
            </p>
          )}
        </div>

        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={() => router.back()}
            className="rounded-lg border border-neutral-300 px-4 py-2 text-sm font-medium hover:bg-neutral-50 dark:border-neutral-700 dark:hover:bg-neutral-800"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {isSubmitting ? "Submitting..." : "Submit Request"}
          </button>
        </div>
      </form>
    </div>
  );
}

function RouteChip({ label, color }: { label: string; color: "amber" | "blue" | "green" }) {
  const styles = {
    amber: "bg-amber-50 border-amber-300 text-amber-700 dark:bg-amber-950 dark:border-amber-700 dark:text-amber-300",
    blue: "bg-blue-50 border-blue-300 text-blue-700 dark:bg-blue-950 dark:border-blue-700 dark:text-blue-300",
    green: "bg-green-50 border-green-300 text-green-700 dark:bg-green-950 dark:border-green-700 dark:text-green-300",
  }[color];

  return <span className={`rounded-full border px-2.5 py-1 text-xs font-medium ${styles}`}>{label}</span>;
}
