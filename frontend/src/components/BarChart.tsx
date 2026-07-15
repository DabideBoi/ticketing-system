export interface BarChartRow {
  label: string;
  value: number;
  colorLight: string;
  colorDark: string;
}

export function BarChart({ rows }: { rows: BarChartRow[] }) {
  const max = Math.max(1, ...rows.map((r) => r.value));

  return (
    <div className="flex flex-col gap-2.5">
      {rows.map((row) => (
        <div key={row.label} className="flex items-center gap-3">
          <span className="w-32 flex-none truncate text-xs text-neutral-600 dark:text-neutral-300">{row.label}</span>
          <div className="h-3.5 flex-1 rounded-full bg-neutral-100 dark:bg-neutral-800">
            <div
              className="h-3.5 rounded-full bg-[var(--bar-color)] dark:bg-[var(--bar-color-dark)] transition-[width]"
              style={
                {
                  width: `${(row.value / max) * 100}%`,
                  "--bar-color": row.colorLight,
                  "--bar-color-dark": row.colorDark,
                } as React.CSSProperties
              }
            />
          </div>
          <span className="w-8 flex-none text-right text-xs font-semibold tabular-nums">{row.value}</span>
        </div>
      ))}
    </div>
  );
}
