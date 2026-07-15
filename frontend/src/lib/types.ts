export type Role = "REQUESTOR" | "APPROVER" | "ASSIGNEE" | "ASSIGNER" | "ADMIN";

export type TicketType =
  | "SERVICE_REQUEST"
  | "DB_FIX"
  | "MASS_USERS"
  | "BCP_USERS"
  | "INCIDENT_REPORT";

export type TicketStatus =
  | "OPEN"
  | "FOR_APPROVAL"
  | "FOR_ASSIGNMENT"
  | "ASSIGNED"
  | "ONGOING"
  | "FOR_CLOSE"
  | "CLOSE";

export const TICKET_STATUS_ORDER: TicketStatus[] = [
  "FOR_APPROVAL",
  "FOR_ASSIGNMENT",
  "ASSIGNED",
  "ONGOING",
  "FOR_CLOSE",
  "CLOSE",
];

export const TICKET_TYPE_LABELS: Record<TicketType, string> = {
  SERVICE_REQUEST: "Service Request",
  DB_FIX: "DB Fix",
  MASS_USERS: "Mass Users",
  BCP_USERS: "BCP Users",
  INCIDENT_REPORT: "Incident Report",
};

export const TICKET_STATUS_LABELS: Record<TicketStatus, string> = {
  OPEN: "Open",
  FOR_APPROVAL: "For Approval",
  FOR_ASSIGNMENT: "For Assignment",
  ASSIGNED: "Assigned",
  ONGOING: "Ongoing",
  FOR_CLOSE: "For Close",
  CLOSE: "Close",
};

export interface User {
  id: string;
  email: string;
  role: Role;
  fullName: string;
}

export interface AuditLog {
  id: string;
  action: string;
  fromStatus: TicketStatus | null;
  toStatus: TicketStatus | null;
  remarks: string | null;
  actionByName: string;
  timestamp: string;
}

export interface Ticket {
  id: string;
  title: string;
  description: string;
  type: TicketType;
  status: TicketStatus;
  requestor: User | null;
  approver: User | null;
  assignee: User | null;
  createdAt: string;
  updatedAt: string;
  auditTrail: AuditLog[];
}

export interface DashboardStats {
  myOpenTickets: number;
  pendingApprovals: number;
  forAssignment: number;
  assignedToMe: number;
  ongoing: number;
}

export interface ReportingSummary {
  statusCounts: Record<TicketStatus, number>;
  typeCounts: Record<TicketType, number>;
  totalTickets: number;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  path: string;
}
