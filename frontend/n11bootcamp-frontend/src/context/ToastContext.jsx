import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
} from "react";

const ToastCtx = createContext(null);

const AUTO_HIDE_MS = 4000;

/**
 * @typedef {{ title: string, subtitle?: string, variant?: 'success' | 'error' }} ToastPayload
 */

function toastBoxClass(variant) {
  if (variant === "error") {
    return "border-rose-200 ring-rose-100";
  }
  return "border-emerald-200 ring-emerald-100";
}

export function ToastProvider({ children }) {
  const [toast, setToast] = useState(null);
  const timerRef = useRef(null);

  const hideToast = useCallback(() => {
    setToast(null);
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  const showToast = useCallback((payload) => {
    const id = Date.now();
    const entry =
      typeof payload === "string"
        ? { id, title: payload, variant: "success" }
        : {
            id,
            title: payload.title,
            subtitle: payload.subtitle,
            variant: payload.variant ?? "success",
          };

    setToast(entry);
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      setToast((t) => (t?.id === id ? null : t));
      timerRef.current = null;
    }, AUTO_HIDE_MS);
  }, []);

  const value = useMemo(
    () => ({ showToast, hideToast }),
    [showToast, hideToast],
  );

  return (
    <ToastCtx.Provider value={value}>
      {children}
      {toast ? (
        <div
          className="pointer-events-none fixed inset-x-0 bottom-0 z-[70] flex justify-center p-4 sm:p-6"
          role="status"
          aria-live="polite"
        >
          <div
            className={`pointer-events-auto flex w-full max-w-md rounded-2xl border bg-white p-4 shadow-xl ring-1 ${toastBoxClass(toast.variant)}`}
          >
            <div className="min-w-0 flex-1 pr-2">
              <div className="text-sm font-extrabold text-slate-900">
                {toast.title}
              </div>
              {toast.subtitle ? (
                <div className="mt-1 text-xs font-medium text-slate-600">
                  {toast.subtitle}
                </div>
              ) : null}
            </div>
            <button
              type="button"
              onClick={hideToast}
              className="shrink-0 rounded-xl px-2 py-1 text-xs font-bold text-slate-500 hover:bg-slate-100 hover:text-slate-800"
              aria-label="Kapat"
            >
              ✕
            </button>
          </div>
        </div>
      ) : null}
    </ToastCtx.Provider>
  );
}

export function useToast() {
  const v = useContext(ToastCtx);
  if (!v) throw new Error("useToast ToastProvider içinde kullanılmalı");
  return v;
}
