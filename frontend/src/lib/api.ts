import type {
  ApiErrorResponse,
  DashboardStats,
  ReportingSummary,
  Role,
  Ticket,
  TicketType,
  User,
} from "./types";
import type { ActivityItem } from "./activity";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const TOKEN_KEY = "ticketing_token";

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  window.localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  window.localStorage.removeItem(TOKEN_KEY);
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string> | undefined),
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    clearToken();
    if (typeof window !== "undefined" && window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
    throw new ApiError(401, "Session expired. Please log in again.");
  }

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body = (await response.json()) as ApiErrorResponse;
      if (body.error) message = body.error;
    } catch {
      // response body wasn't JSON; keep the default message
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export function login(email: string, password: string) {
  return apiFetch<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function getTickets() {
  return apiFetch<Ticket[]>("/api/tickets");
}

export function getTicket(id: string) {
  return apiFetch<Ticket>(`/api/tickets/${id}`);
}

export function createTicket(payload: { title: string; description: string; type: TicketType }) {
  return apiFetch<Ticket>("/api/tickets", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function approveTicket(id: string, payload: { approved: boolean; remarks?: string }) {
  return apiFetch<Ticket>(`/api/tickets/${id}/approve`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function assignTicket(id: string, assigneeId: string) {
  return apiFetch<Ticket>(`/api/tickets/${id}/assign`, {
    method: "POST",
    body: JSON.stringify({ assigneeId }),
  });
}

export function updateTicketStatus(id: string, status: string) {
  return apiFetch<Ticket>(`/api/tickets/${id}/status`, {
    method: "PUT",
    body: JSON.stringify({ status }),
  });
}

export function getDashboardStats() {
  return apiFetch<DashboardStats>("/api/dashboard/stats");
}

export function getDashboardActivity() {
  return apiFetch<ActivityItem[]>("/api/dashboard/activity");
}

export function getReportingSummary() {
  return apiFetch<ReportingSummary>("/api/reporting/summary");
}

export function getUsersByRole(role: Role) {
  return apiFetch<User[]>(`/api/users?role=${role}`);
}
