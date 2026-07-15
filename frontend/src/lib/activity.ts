import type { TicketStatus, TicketType } from "@/lib/types";

export interface ActivityItem {
  ticketId: string;
  ticketTitle: string;
  ticketType: TicketType;
  fromStatus: TicketStatus | null;
  toStatus: TicketStatus | null;
  actionByName: string;
  timestamp: string;
}
