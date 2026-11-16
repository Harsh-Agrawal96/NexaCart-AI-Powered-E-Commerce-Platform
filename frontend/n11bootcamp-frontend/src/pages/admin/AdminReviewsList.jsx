import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { adminDeleteReview, adminSearchReviews } from "../../services/adminReviewService";

function Pill({ ok, children }) {
  return (
    <span
      className={[
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-extrabold ring-1",
        ok
          ? "bg-emerald-50 text-emerald-800 ring-emerald-200"
          : "bg-slate-100 text-slate-700 ring-slate-200",
      ].join(" ")}
    >
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

function getReviewerName(r) {
  const first = String(r?.userFirstName ?? "").trim();
  const last = String(r?.userLastName ?? "").trim();
  const full = `${first} ${last}`.trim();
  return full || "Anonim";
}

export default function AdminReviewsList() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  const [filters, setFilters] = useState({
    productId: "",
    userId: "",
    ratingMin: "",
    ratingMax: "",
  });

  const [page, setPage] = useState({
    content: [],
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const rows = page?.content ?? [];

  const metaText = useMemo(() => {
    return `${page.page + 1} / ${Math.max(1, page.totalPages)} • ${page.totalElements} yorum`;
  }, [page.page, page.totalElements, page.totalPages]);

  async function load(nextPage = page.page) {
    setLoading(true);
    setError("");
    try {
      const params = { page: nextPage, size: page.size, sort: "createdAt,desc" };
      if (filters.productId.trim()) params.productId = filters.productId.trim();
      if (filters.userId.trim()) params.userId = filters.userId.trim();
      if (filters.ratingMin !== "") params.ratingMin = Number(filters.ratingMin);
      if (filters.ratingMax !== "") params.ratingMax = Number(filters.ratingMax);

      const p = await adminSearchReviews(params);
      setPage(
        p ?? {
          content: [],
          page: 0,
          size: page.size,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        },
      );
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Yorumlar yüklenemedi";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">Yorumlar</h1>
          <p className="mt-1 text-sm text-slate-600">{metaText}</p>
        </div>
      </div>

      <div className="rounded-3xl border border-slate-200 bg-white p-5">
        <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-5">
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">productId</div>
            <input
              value={filters.productId}
              onChange={(e) => setFilters((s) => ({ ...s, productId: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              placeholder="uuid"
            />
          </div>
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">userId</div>
            <input
              value={filters.userId}
              onChange={(e) => setFilters((s) => ({ ...s, userId: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              placeholder="uuid"
            />
          </div>
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">ratingMin</div>
            <input
              value={filters.ratingMin}
              onChange={(e) => setFilters((s) => ({ ...s, ratingMin: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              inputMode="numeric"
              placeholder="1"
            />
          </div>
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">ratingMax</div>
            <input
              value={filters.ratingMax}
              onChange={(e) => setFilters((s) => ({ ...s, ratingMax: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              inputMode="numeric"
              placeholder="5"
            />
          </div>
          <div className="flex items-end gap-2">
            <button
              type="button"
              onClick={() => load(0)}
              className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
            >
              Uygula
            </button>
          </div>
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
                  <th className="px-4 py-3">Yorum</th>
                  <th className="px-4 py-3">Ürün</th>
                  <th className="px-4 py-3">Kullanıcı</th>
                  <th className="px-4 py-3">Puan</th>
                  <th className="px-4 py-3">Tarih</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {rows.map((r) => (
                  <tr key={r.id} className="align-top hover:bg-slate-50/50">
                    <td className="px-4 py-3">
                      <div className="max-w-[520px] whitespace-pre-wrap break-words text-slate-800">
                        {r.comment || "—"}
                      </div>
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-slate-600">{r.productId}</td>
                    <td className="px-4 py-3">
                      <div className="font-extrabold text-slate-900">{getReviewerName(r)}</div>
                      {r.userId ? (
                        <div className="mt-1 font-mono text-[11px] text-slate-400">{r.userId}</div>
                      ) : null}
                    </td>
                    <td className="px-4 py-3">
                      <Pill ok>{r.rating} / 5</Pill>
                    </td>
                    <td className="px-4 py-3 text-slate-600">{fmtDate(r.createdAt)}</td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <Link
                          to={`/admin/reviews/${r.id}`}
                          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50"
                        >
                          Detay
                        </Link>
                        <button
                          type="button"
                          disabled={busyId === r.id}
                          onClick={async () => {
                            if (!confirm("Yorum silinsin mi?")) return;
                            setBusyId(r.id);
                            try {
                              await adminDeleteReview(r.id);
                              await load(page.page);
                            } finally {
                              setBusyId(null);
                            }
                          }}
                          className="inline-flex items-center justify-center rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-extrabold text-rose-700 hover:bg-rose-100 disabled:opacity-50"
                        >
                          {busyId === r.id ? "..." : "Sil"}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between gap-2 border-t border-slate-200 bg-white px-4 py-3">
            <button
              type="button"
              disabled={page.first}
              onClick={() => load(Math.max(0, page.page - 1))}
              className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
            >
              Önceki
            </button>
            <div className="text-xs font-semibold text-slate-600">{metaText}</div>
            <button
              type="button"
              disabled={page.last}
              onClick={() => load(page.page + 1)}
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

