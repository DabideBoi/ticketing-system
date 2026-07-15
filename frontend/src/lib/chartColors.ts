import type { TicketStatus, TicketType } from "@/lib/types";

// Categorical palette (fixed slot order), validated with the dataviz skill's
// validate_palette.js — passes lightness/chroma/CVD-separation/contrast checks
// for both light and dark chart surfaces.
export const TICKET_TYPE_COLORS: Record<TicketType, { light: string; dark: string }> = {
  SERVICE_REQUEST: { light: "#2a78d6", dark: "#3987e5" }, // slot 1: blue
  DB_FIX: { light: "#1baf7a", dark: "#199e70" }, // slot 2: aqua
  MASS_USERS: { light: "#eda100", dark: "#c98500" }, // slot 3: yellow
  BCP_USERS: { light: "#008300", dark: "#008300" }, // slot 4: green
  INCIDENT_REPORT: { light: "#4a3aa7", dark: "#9085e9" }, // slot 5: violet
};

// Sequential single-hue (blue) ramp, ordinal steps for the 7 ordered workflow
// stages — lightest step stays at/above the 2:1-floor step (250) on light.
const SEQUENTIAL_BLUE_STEPS = ["#86b6ef", "#6da7ec", "#5598e7", "#2a78d6", "#1c5cab", "#184f95", "#104281"];

export const TICKET_STATUS_ORDER_FOR_CHART: TicketStatus[] = [
  "OPEN",
  "FOR_APPROVAL",
  "FOR_ASSIGNMENT",
  "ASSIGNED",
  "ONGOING",
  "FOR_CLOSE",
  "CLOSE",
];

export const TICKET_STATUS_COLORS: Record<TicketStatus, { light: string; dark: string }> = Object.fromEntries(
  TICKET_STATUS_ORDER_FOR_CHART.map((status, i) => [status, { light: SEQUENTIAL_BLUE_STEPS[i], dark: SEQUENTIAL_BLUE_STEPS[i] }]),
) as Record<TicketStatus, { light: string; dark: string }>;
