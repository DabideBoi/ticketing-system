"use client";

import { useEffect } from "react";
import { AlertTriangle } from "lucide-react";

export default function Error({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="flex min-h-screen items-center justify-center p-6">
      <div className="max-w-md w-full rounded-xl border border-red-300 bg-red-50 p-6 text-center dark:border-red-800 dark:bg-red-950">
        <AlertTriangle className="mx-auto mb-3 h-10 w-10 text-red-600 dark:text-red-400" />
        <h2 className="text-lg font-semibold text-red-800 dark:text-red-200">Something went wrong</h2>
        <p className="mt-1 text-sm text-red-700 dark:text-red-300">{error.message || "An unexpected error occurred."}</p>
        <button
          onClick={reset}
          className="mt-4 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
        >
          Try again
        </button>
      </div>
    </div>
  );
}
