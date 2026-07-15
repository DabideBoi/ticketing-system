export function KpiChip({ label, value, highlight }: { label: string; value: number; highlight?: boolean }) {
  return (
    <div
      className={`flex-1 rounded-lg border p-3 ${
        highlight
          ? "border-blue-300 bg-blue-50 dark:border-blue-700 dark:bg-blue-950"
          : "border-neutral-200 bg-white dark:border-neutral-800 dark:bg-neutral-900"
      }`}
    >
      <div className={`text-2xl font-bold ${highlight ? "text-blue-700 dark:text-blue-300" : ""}`}>{value}</div>
      <div className="text-xs text-neutral-500 dark:text-neutral-400">{label}</div>
    </div>
  );
}
