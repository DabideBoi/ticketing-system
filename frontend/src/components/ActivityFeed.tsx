import Link from "next/link";
import type { ActivityItem } from "@/lib/activity";
import { formatRelativeTime } from "@/lib/format";
import { TICKET_STATUS_LABELS, TICKET_TYPE_LABELS } from "@/lib/types";

export function ActivityFeed({ items }: { items: ActivityItem[] }) {
  if (items.length === 0) {
    return (
      <div className="rounded-lg border border-dashed border-neutral-300 p-8 text-center text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-400">
        Nothing to review right now.
      </div>
    );
  }

  return (
    <ol className="flex flex-col gap-4 border-l-2 border-blue-500 pl-4">
      {items.map((item, index) => (
        <li key={`${item.ticketId}-${item.timestamp}-${index}`} className="relative">
          <span className="absolute -left-[21px] top-1.5 h-2.5 w-2.5 rounded-full border-2 border-white bg-blue-500 shadow-[0_0_0_1.5px_theme(colors.blue.500)] dark:border-neutral-950" />
          <Link href={`/tickets/${item.ticketId}`} className="block hover:opacity-80">
            <p className="text-sm font-medium">
              {item.ticketTitle}{" "}
              <span className="font-normal text-neutral-500 dark:text-neutral-400">
                ({TICKET_TYPE_LABELS[item.ticketType]})
                {item.toStatus ? ` → ${TICKET_STATUS_LABELS[item.toStatus]}` : ""}
              </span>
            </p>
            <p className="text-xs text-neutral-500 dark:text-neutral-400">
              by {item.actionByName} · {formatRelativeTime(item.timestamp)}
            </p>
          </Link>
        </li>
      ))}
    </ol>
  );
}
