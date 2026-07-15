import type { AuditLog } from "@/lib/types";
import { TICKET_STATUS_LABELS } from "@/lib/types";
import { formatRelativeTime } from "@/lib/format";

export function AuditTrail({ entries }: { entries: AuditLog[] }) {
  if (entries.length === 0) {
    return <p className="text-sm text-neutral-500 dark:text-neutral-400">No activity recorded yet.</p>;
  }

  return (
    <ol className="flex flex-col gap-4 border-l-2 border-neutral-300 pl-4 dark:border-neutral-700">
      {entries.map((entry) => (
        <li key={entry.id} className="relative">
          <span className="absolute -left-[21px] top-1.5 h-2.5 w-2.5 rounded-full border-2 border-white bg-neutral-400 dark:border-neutral-950 dark:bg-neutral-500" />
          <p className="text-sm font-medium">
            {entry.toStatus ? `Status → ${TICKET_STATUS_LABELS[entry.toStatus]}` : entry.action}
          </p>
          <p className="text-xs text-neutral-500 dark:text-neutral-400">
            by {entry.actionByName} · {formatRelativeTime(entry.timestamp)}
          </p>
          {entry.remarks && (
            <p className="mt-1 rounded-md bg-neutral-100 px-2 py-1 text-xs text-neutral-600 dark:bg-neutral-800 dark:text-neutral-300">
              &ldquo;{entry.remarks}&rdquo;
            </p>
          )}
        </li>
      ))}
    </ol>
  );
}
