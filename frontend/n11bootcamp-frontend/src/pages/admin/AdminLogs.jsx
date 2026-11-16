import { useEffect, useMemo, useState } from "react";
import {
  adminGetLogs,
  adminGetLogsByOrderId,
  adminGetLogsByServiceName,
  adminGetLogsByUserId,
} from "../../services/adminLogService";

function Badge({ variant, children }) {
  const cls =
    variant === "error"
      ? "bg-rose-50 text-rose-800 ring-rose-200"
      : variant === "warn"
        ? "bg-amber-50 text-amber-800 ring-amber-200"
        : variant === "info"
          ? "bg-sky-50 text-sky-800 ring-sky-200"
          : "bg-slate-100 text-slate-800 ring-slate-200";
  return (
    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-extrabold ring-1 ${cls}`}>
      {children}
    </span>
  );
}

function fmtDate(v) {
  if (!v) return "—";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return d.toLocaleString("tr-TR");
}

/** Backend created-date alanı: OpenAPI `createdAt`; servis `createdDate` / `timestamp` dönebilir. */
function getLogCreatedRaw(x) {
  return x?.createdAt ?? x?.createdDate ?? x?.timestamp ?? null;
}

function getLogCreatedMs(x) {
  const raw = getLogCreatedRaw(x);
  if (!raw) return 0;
  const t = new Date(raw).getTime();
  return Number.isNaN(t) ? 0 : t;
}

const EVENT_TYPES = [
  "",
  "ORDER_CREATED",
  "INVENTORY_RESERVED",
  "INVENTORY_FAILED",
  "PAYMENT_SUCCEEDED",
  "PAYMENT_FAILED",
  "APPLICATION_LOG",
];
const LEVELS = ["", "INFO", "WARN", "ERROR"];

export default function AdminLogs() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [queryType, setQueryType] = useState("all"); // all|user|service|order
  const [queryValue, setQueryValue] = useState("");

  const [eventType, setEventType] = useState("");
  const [level, setLevel] = useState("");

  const [page, setPage] = useState({
    content: [],
    number: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    empty: true,
  });

  const metaText = useMemo(() => {
    const cur = page.number + 1;
    const total = Math.max(1, page.totalPages || 0);
    return `${cur} / ${total} • ${page.totalElements} kayıt`;
  }, [page.number, page.totalElements, page.totalPages]);

  async function load(nextPage = page.number) {
    setLoading(true);
    setError("");

    try {
      const pageArgs = { page: nextPage, size: page.size };
      const v = queryValue.trim();

      let res;
      if (queryType === "user") {
        res = await adminGetLogsByUserId(v, pageArgs);
      } else if (queryType === "service") {
        res = await adminGetLogsByServiceName(v, pageArgs);
      } else if (queryType === "order") {
        res = await adminGetLogsByOrderId(v, pageArgs);
      } else {
        res = await adminGetLogs(pageArgs);
      }

      setPage(
        res ?? {
          content: [],
          number: 0,
          size: page.size,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
          empty: true,
        },
      );
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Loglar yüklenemedi";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredContent = useMemo(() => {
    const content = page.content ?? [];
    const filtered = content.filter((x) => {
      if (eventType && x.eventType !== eventType) return false;
      if (level && x.level !== level) return false;
      return true;
    });
    // Sayfa içi tutarlılık: API sırası genelde created date; filtre sonrası yeniden eskiye.
    return [...filtered].sort((a, b) => getLogCreatedMs(b) - getLogCreatedMs(a));
  }, [eventType, level, page.content]);

  const valuePlaceholder =
    queryType === "user"
      ? "userId"
      : queryType === "service"
        ? "serviceName (örn: order-service)"
        : queryType === "order"
          ? "orderId"
          : "";

  const valueRequired = queryType !== "all";

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">Loglar</h1>
          <p className="mt-1 text-sm text-slate-600">{metaText}</p>
        </div>
      </div>

      <div className="rounded-3xl border border-slate-200 bg-white p-5">
        <div className="grid gap-3 md:grid-cols-4">
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">Sorgu türü</div>
            <select
              value={queryType}
              onChange={(e) => {
                setQueryType(e.target.value);
                setQueryValue("");
              }}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            >
              <option value="all">Tümü</option>
              <option value="user">UserId</option>
              <option value="service">ServiceName</option>
              <option value="order">OrderId</option>
            </select>
          </div>

          <div className="space-y-1 md:col-span-2">
            <div className="text-xs font-semibold text-slate-600">Değer</div>
            <input
              value={queryValue}
              onChange={(e) => setQueryValue(e.target.value)}
              disabled={queryType === "all"}
              placeholder={valuePlaceholder}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200 disabled:bg-slate-50"
              required={valueRequired}
            />
          </div>

          <div className="flex items-end gap-2">
            <button
              type="button"
              onClick={() => load(0)}
              disabled={valueRequired && !queryValue.trim()}
              className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800 disabled:opacity-50"
            >
              Ara
            </button>
          </div>
        </div>

        <div className="mt-4 grid gap-3 md:grid-cols-4">
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">Event Type</div>
            <select
              value={eventType}
              onChange={(e) => setEventType(e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            >
              {EVENT_TYPES.map((x) => (
                <option key={x} value={x}>
                  {x || "Hepsi"}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">Level</div>
            <select
              value={level}
              onChange={(e) => setLevel(e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            >
              {LEVELS.map((x) => (
                <option key={x} value={x}>
                  {x || "Hepsi"}
                </option>
              ))}
            </select>
          </div>

          <div className="md:col-span-2 flex items-end justify-end">
            <button
              type="button"
              onClick={() => {
                setEventType("");
                setLevel("");
              }}
              className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-extrabold text-slate-800 hover:bg-slate-50"
            >
              Filtreleri temizle
            </button>
          </div>
        </div>

        <div className="mt-4 text-xs text-slate-500">
          Not: `eventType/level` filtreleri client-side uygulanıyor (mevcut endpoint’lerde query param yok).
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
        <div className="overflow-hidden rounded-3xl border border-slate-200 bg-white">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs font-extrabold uppercase tracking-wide text-slate-600">
                <tr>
                  <th className="px-4 py-3">Oluşturulma</th>
                  <th className="px-4 py-3">Servis</th>
                  <th className="px-4 py-3">Event</th>
                  <th className="px-4 py-3">Level</th>
                  <th className="px-4 py-3">Mesaj</th>
                  <th className="px-4 py-3">İlişki</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {filteredContent.map((x) => (
                  <tr key={x.id} className="align-top hover:bg-slate-50/50">
                    <td className="px-4 py-3 whitespace-nowrap text-slate-700">
                      {fmtDate(getLogCreatedRaw(x))}
                    </td>
                    <td className="px-4 py-3 text-slate-800">
                      <div className="font-extrabold">{x.serviceName ?? "—"}</div>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant="neutral">{x.eventType ?? "—"}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <Badge
                        variant={
                          x.level === "ERROR"
                            ? "error"
                            : x.level === "WARN"
                              ? "warn"
                              : x.level === "INFO"
                                ? "info"
                                : "neutral"
                        }
                      >
                        {x.level ?? "—"}
                      </Badge>
                    </td>
                    <td className="px-4 py-3 text-slate-800">
                      <div className="max-w-[520px] whitespace-pre-wrap break-words">
                        {x.message ?? "—"}
                      </div>
                      {x.payload ? (
                        <details className="mt-2">
                          <summary className="cursor-pointer select-none text-xs font-extrabold text-slate-700">
                            payload
                          </summary>
                          <pre className="mt-2 overflow-x-auto rounded-2xl border border-slate-200 bg-slate-50 p-3 text-xs text-slate-700">
{x.payload}
                          </pre>
                        </details>
                      ) : null}
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-700">
                      <div className="space-y-1">
                        <div>
                          <span className="font-semibold text-slate-500">userId:</span>{" "}
                          <span className="font-mono">{x.userId ?? "—"}</span>
                        </div>
                        <div>
                          <span className="font-semibold text-slate-500">orderId:</span>{" "}
                          <span className="font-mono">{x.orderId ?? "—"}</span>
                        </div>
                      </div>
                    </td>
                  </tr>
                ))}

                {filteredContent.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-4 py-8 text-center text-sm text-slate-600">
                      Kayıt yok.
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between gap-2 border-t border-slate-200 bg-white px-4 py-3">
            <button
              type="button"
              disabled={page.first}
              onClick={() => load(Math.max(0, page.number - 1))}
              className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
            >
              Önceki
            </button>
            <div className="text-xs font-semibold text-slate-600">{metaText}</div>
            <button
              type="button"
              disabled={page.last}
              onClick={() => load(page.number + 1)}
              className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
            >
              Sonraki
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

