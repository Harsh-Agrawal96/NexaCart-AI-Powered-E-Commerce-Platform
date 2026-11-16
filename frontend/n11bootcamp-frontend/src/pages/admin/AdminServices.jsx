import { useEffect, useMemo, useRef, useState } from "react";
import {
  getGatewayHealth,
  getGatewayInfo,
  getGatewayRoutes,
  pingApiDocs,
} from "../../services/adminServiceStatusService";

const services = [
  { name: "user-service", apiDocs: "/user-service/api-docs" },
  { name: "product-service", apiDocs: "/product-service/api-docs" },
  { name: "cart-service", apiDocs: "/cart-service/api-docs" },
  { name: "order-service", apiDocs: "/order-service/api-docs" },
  { name: "payment-service", apiDocs: "/payment-service/api-docs" },
  { name: "review-service", apiDocs: "/review-service/api-docs" },
  { name: "ai-service", apiDocs: "/ai-service/api-docs" },
  { name: "log-service", apiDocs: "/log-service/api-docs" },
];

function Pill({ ok, children }) {
  return (
    <span
      className={[
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-extrabold ring-1",
        ok
          ? "bg-emerald-50 text-emerald-800 ring-emerald-200"
          : "bg-rose-50 text-rose-700 ring-rose-200",
      ].join(" ")}
    >
      {children}
    </span>
  );
}

const AUTO_REFRESH_MS = 15_000;

export default function AdminServices() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [autoRefresh, setAutoRefresh] = useState(true);

  const [health, setHealth] = useState(null);
  const [info, setInfo] = useState(null);
  const [routes, setRoutes] = useState(null);
  const [checks, setChecks] = useState({});
  const [lastRunAt, setLastRunAt] = useState(null);
  const ticking = useRef(false);

  const routeCount = useMemo(() => {
    if (!routes) return 0;
    if (Array.isArray(routes)) return routes.length;
    if (Array.isArray(routes?.routes)) return routes.routes.length;
    return 0;
  }, [routes]);

  async function load({ silent = false } = {}) {
    if (!silent) {
      setLoading(true);
      setError("");
    }
    try {
      const [h, i, r] = await Promise.all([
        getGatewayHealth(),
        getGatewayInfo(),
        getGatewayRoutes(),
      ]);
      setHealth(h);
      setInfo(i);
      setRoutes(r);
      setLastRunAt(new Date());
      if (!silent) setError("");
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Gateway actuator okunamadı";
      setError(msg);
    } finally {
      if (!silent) setLoading(false);
    }
  }

  async function runChecks() {
    const next = {};
    await Promise.all(
      services.map(async (s) => {
        try {
          await pingApiDocs(s.apiDocs);
          next[s.name] = { ok: true, detail: "OK" };
        } catch (e) {
          const status = e?.response?.status;
          const code = e?.code;
          let detail = status ? `HTTP ${status}` : "ERR";
          if (!status && (code === "ERR_NETWORK" || e?.message === "Network Error")) {
            detail = "Ağ (gateway kapalı / proxy yok)";
          }
          next[s.name] = { ok: false, detail };
        }
      }),
    );
    setChecks(next);
    setLastRunAt(new Date());
  }

  async function refreshAll({ silent = false } = {}) {
    if (ticking.current) return;
    ticking.current = true;
    try {
      await load({ silent });
      await runChecks();
    } finally {
      ticking.current = false;
    }
  }

  useEffect(() => {
    refreshAll({ silent: false });
  }, []);

  useEffect(() => {
    if (!autoRefresh) return undefined;
    const id = window.setInterval(() => {
      refreshAll({ silent: true });
    }, AUTO_REFRESH_MS);
    return () => window.clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoRefresh]);

  const gatewayUp =
    String(health?.status ?? "").toUpperCase() === "UP" ||
    String(health?.data?.status ?? "").toUpperCase() === "UP";

  const lastRunLabel = lastRunAt
    ? lastRunAt.toLocaleTimeString("tr-TR", { hour: "2-digit", minute: "2-digit", second: "2-digit" })
    : "—";

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            Servis Durumu
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            Gateway actuator + gateway üzerinden her servisin{" "}
            <span className="font-mono text-xs">/…-service/api-docs</span> ping’i.
          </p>
          <p className="mt-2 text-xs text-slate-500">
            <strong className="font-semibold text-slate-600">Gateway UP</strong> yalnızca API
            Gateway sürecinin ayakta olduğunu gösterir; bir mikroservisi durdurduğunuzda bu satır
            çoğu zaman yine UP kalır. Hangi servisin düştüğünü{" "}
            <strong className="font-semibold text-slate-600">Servis Ping</strong> satırlarından
            görürsünüz (502/503 → kırmızı). Son kontrol:{" "}
            <span className="font-mono">{lastRunLabel}</span>
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <label className="flex cursor-pointer items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-700">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
              className="rounded border-slate-300"
            />
            Otomatik ({AUTO_REFRESH_MS / 1000}s)
          </label>
          <button
            type="button"
            onClick={() => refreshAll()}
            className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
          >
            Yenile
          </button>
          <button
            type="button"
            onClick={() => runChecks()}
            className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-3 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
          >
            Sadece ping
          </button>
        </div>
      </div>

      {loading ? (
        <div className="rounded-3xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
          Yükleniyor...
        </div>
      ) : error ? (
        <div className="rounded-3xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
          {error}
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-12">
          <div className="space-y-4 lg:col-span-5">
            <div className="rounded-3xl border border-slate-200 bg-white p-6">
              <div className="flex items-center justify-between gap-3">
                <div className="text-sm font-extrabold text-slate-900">Gateway</div>
                <Pill ok={gatewayUp}>{gatewayUp ? "UP" : "DOWN"}</Pill>
              </div>
              <div className="mt-4 grid gap-3 sm:grid-cols-2">
                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                  <div className="text-xs font-semibold text-slate-500">
                    Routes
                  </div>
                  <div className="mt-1 text-lg font-extrabold text-slate-900">
                    {routeCount}
                  </div>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                  <div className="text-xs font-semibold text-slate-500">Info</div>
                  <div className="mt-1 truncate font-mono text-xs text-slate-700">
                    {info ? "OK" : "—"}
                  </div>
                </div>
              </div>
              <details className="mt-4 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700">
                <summary className="cursor-pointer select-none text-xs font-extrabold text-slate-700">
                  /actuator/health (raw)
                </summary>
                <pre className="mt-3 overflow-x-auto text-xs">
{JSON.stringify(health, null, 2)}
                </pre>
              </details>
            </div>

            <div className="rounded-3xl border border-slate-200 bg-white p-6">
              <div className="text-sm font-extrabold text-slate-900">
                Servis Ping
              </div>
              <div className="mt-3 space-y-2">
                {services.map((s) => {
                  const c = checks[s.name];
                  return (
                    <div
                      key={s.name}
                      className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white px-4 py-3"
                    >
                      <div className="min-w-0">
                        <div className="truncate text-sm font-extrabold text-slate-900">
                          {s.name}
                        </div>
                        <div className="truncate font-mono text-xs text-slate-500">
                          {s.apiDocs}
                        </div>
                      </div>
                      <Pill ok={c?.ok}>{c?.detail ?? "—"}</Pill>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          <div className="space-y-4 lg:col-span-7">
            <div className="rounded-3xl border border-slate-200 bg-white p-6">
              <div className="text-sm font-extrabold text-slate-900">
                /actuator/gateway/routes (raw)
              </div>
              <pre className="mt-3 max-h-[520px] overflow-auto rounded-2xl border border-slate-200 bg-slate-50 p-4 text-xs text-slate-800">
{JSON.stringify(routes, null, 2)}
              </pre>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

