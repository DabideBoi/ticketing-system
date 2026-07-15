import type { ProgressStep } from "@/lib/progress";
import { TICKET_STATUS_LABELS } from "@/lib/types";

const DOT_STYLES = {
  done: "bg-green-500 border-green-600",
  active: "bg-blue-600 border-blue-700 h-4 w-4",
  pending: "bg-neutral-200 border-neutral-300 dark:bg-neutral-700 dark:border-neutral-600",
};

const LINE_STYLES = {
  done: "bg-green-500",
  active: "bg-neutral-200 dark:bg-neutral-700",
  pending: "bg-neutral-200 dark:bg-neutral-700",
};

export function ProgressRail({ steps }: { steps: ProgressStep[] }) {
  return (
    <div className="flex gap-3">
      <div className="flex flex-col items-center pt-1">
        {steps.map((step, i) => (
          <div key={step.status} className="flex flex-col items-center">
            <div className={`h-3 w-3 rounded-full border-2 ${DOT_STYLES[step.state]}`} />
            {i < steps.length - 1 && <div className={`w-0.5 flex-1 ${LINE_STYLES[step.state]}`} style={{ minHeight: 28 }} />}
          </div>
        ))}
      </div>
      <div className="flex flex-col pb-1" style={{ gap: 12 }}>
        {steps.map((step) => (
          <div key={step.status} className={step.state === "pending" ? "opacity-50" : ""} style={{ minHeight: 28 }}>
            <p className={`text-sm ${step.state === "active" ? "font-semibold text-blue-700 dark:text-blue-400" : "font-medium"}`}>
              {step.label ?? TICKET_STATUS_LABELS[step.status]}
              {step.state === "active" && " ●"}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
